package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.entity.DeferredTaskEntity
import com.example.data.entity.FocusSessionEntity
import com.example.data.entity.GoalEntity
import com.example.data.entity.SessionWithDetails
import com.example.data.entity.TimelineBlockEntity
import com.example.data.repository.FocusRepository
import com.example.domain.model.DailySummary
import com.example.domain.model.FocusState
import com.example.domain.model.color
import com.example.domain.model.OverallAnalytics
import com.example.domain.model.PomodoroPhase
import com.example.domain.model.ThemeMode
import com.example.domain.model.ThemePreset
import com.example.domain.model.TimerMode
import com.example.domain.util.FocusNotificationHelper
import com.example.domain.util.MarkdownExporter
import com.example.domain.util.ParsedMarkdownSession
import com.example.domain.util.AlarmHelper
import com.example.domain.util.TimeFormatter
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class FocusViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: FocusRepository
    private val _uiState = MutableStateFlow(FocusUiState())
    val uiState: StateFlow<FocusUiState> = _uiState.asStateFlow()

    private val _analyticsPeriod = MutableStateFlow(AnalyticsPeriod.WEEK)
    private val _analyticsOffset = MutableStateFlow(0)

    private var timerJob: Job? = null
    private var secondCounter: Int = 0

    private val prefs = application.getSharedPreferences("FocusSettings", android.content.Context.MODE_PRIVATE)

    init {
        // Load settings
        val savedThemePreset = ThemePreset.valueOf(prefs.getString("theme_preset", ThemePreset.MIDNIGHT_OBSIDIAN.name) ?: ThemePreset.MIDNIGHT_OBSIDIAN.name)
        val savedThemeMode = ThemeMode.valueOf(prefs.getString("theme_mode", ThemeMode.SYSTEM.name) ?: ThemeMode.SYSTEM.name)
        val savedTimerMode = TimerMode.valueOf(prefs.getString("timer_mode", TimerMode.STOPWATCH.name) ?: TimerMode.STOPWATCH.name)
        val savedWorkMin = prefs.getInt("pomodoro_work", 25)
        val savedBreakMin = prefs.getInt("pomodoro_break", 5)

        _uiState.update { it.copy(
            themePreset = savedThemePreset,
            themeMode = savedThemeMode,
            timerMode = savedTimerMode,
            pomodoroWorkMinutes = savedWorkMin,
            pomodoroShortBreakMinutes = savedBreakMin
        ) }

        val db = AppDatabase.getDatabase(application)
        repository = FocusRepository(db.focusDao())
        FocusNotificationHelper.createNotificationChannel(application)

        // Observe active and carried-over goals
        viewModelScope.launch {
            repository.activeGoals.collect { goals ->
                _uiState.update { current ->
                    val mappedGoals = goals.map { goal ->
                        if (current.isSessionActive && goal.createdAtTimestamp < current.sessionStartTime) {
                            goal.copy(isCarriedOver = true)
                        } else {
                            goal
                        }
                    }
                    val usedGroups = mappedGoals.map { it.groupName }.filter { it.isNotBlank() }.distinct()
                    current.copy(
                        activeGoals = mappedGoals,
                        availableGoalGroups = listOf("All") + usedGroups
                    )
                }
            }
        }

        // Observe all deferred tasks (mind dump)
        viewModelScope.launch {
            repository.allDeferredTasks.collect { tasks ->
                val usedGroups = tasks.map { it.groupName }.filter { it.isNotBlank() }.distinct()
                _uiState.update { it.copy(
                    deferredTasks = tasks,
                    availableMindDumpGroups = listOf("All") + usedGroups
                ) }
            }
        }

        // Observe sessions and calculate analytics based on selected period
        viewModelScope.launch {
            kotlinx.coroutines.flow.combine(
                repository.allSessionsWithDetails,
                _analyticsPeriod,
                _analyticsOffset
            ) { sessions, period, offset ->
                com.example.ui.viewmodel.AnalyticsData(sessions, period, offset)
            }.collect { data ->
                val sessions = data.sessions
                val period = data.period
                val offset = data.offset
                val filteredSessions = filterSessionsByPeriod(sessions, period, offset)
                val analytics = calculateAnalytics(filteredSessions, period, offset) // Compute analytics for the filtered range
                
                // We still want currentStreak to be global, so let's compute streak from ALL sessions
                val globalAnalytics = calculateAnalytics(sessions, AnalyticsPeriod.WEEK, 0)
                
                _uiState.update {
                    it.copy(
                        pastSessions = sessions, // keep all for history, or just filtered?
                        overallAnalytics = analytics.copy(currentStreak = globalAnalytics.currentStreak),
                        analyticsPeriod = period,
                        analyticsOffset = offset
                    )
                }
            }
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isActive) {
                delay(1000L)
                val current = _uiState.value
                if (current.isSessionActive && current.currentState != FocusState.Idle) {
                    val now = System.currentTimeMillis()
                    val blockElapsed = (now - current.blockStartTime) / 1000L
                    val totalElapsed = (now - current.sessionStartTime) / 1000L
                    secondCounter++

                    // Track work time on selected goal if in Working state
                    val updatedGoals = if (current.currentState == FocusState.Working && current.selectedGoalId != null) {
                        current.activeGoals.map { goal ->
                            if (goal.id == current.selectedGoalId) {
                                val updatedSpent = goal.totalWorkSecondsSpent + 1
                                goal.copy(totalWorkSecondsSpent = updatedSpent)
                            } else {
                                goal
                            }
                        }
                    } else {
                        current.activeGoals
                    }

                    // Periodically sync goal time to Room DB every 5 seconds
                    if (secondCounter % 5 == 0 && current.selectedGoalId != null && current.currentState == FocusState.Working) {
                        val activeGoal = updatedGoals.find { it.id == current.selectedGoalId }
                        if (activeGoal != null) {
                            repository.updateGoal(activeGoal)
                        }
                    }

                    if (current.timerMode == TimerMode.POMODORO) {
                        val newRemaining = maxOf(0L, (current.pomodoroPhaseEndTime - now) / 1000L)
                        if (newRemaining <= 0 && current.pomodoroPhaseEndTime > 0) {
                            // Phase completed! Advance pomodoro phase automatically
                            handlePomodoroPhaseCompleted()
                        } else {
                            _uiState.update {
                                it.copy(
                                    currentBlockElapsedSeconds = maxOf(0L, blockElapsed),
                                    totalSessionElapsedSeconds = maxOf(0L, totalElapsed),
                                    pomodoroRemainingSeconds = newRemaining,
                                    activeGoals = updatedGoals
                                )
                            }
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                currentBlockElapsedSeconds = maxOf(0L, blockElapsed),
                                totalSessionElapsedSeconds = maxOf(0L, totalElapsed),
                                activeGoals = updatedGoals
                            )
                        }
                    }

                    // Update Android Notification Shade
                    val latest = _uiState.value
                    val timerDigits = if (latest.timerMode == TimerMode.POMODORO) {
                        TimeFormatter.formatDuration(latest.pomodoroRemainingSeconds)
                    } else {
                        TimeFormatter.formatDuration(latest.currentBlockElapsedSeconds)
                    }
                    val selectedGoalObj = latest.activeGoals.find { it.id == latest.selectedGoalId }
                    val cycleSubtitle = if (latest.timerMode == TimerMode.POMODORO) {
                        "Cycle ${latest.pomodoroCurrentCycle}/${latest.pomodoroTargetCycles}"
                    } else {
                        "Session: ${TimeFormatter.formatDuration(latest.totalSessionElapsedSeconds)}"
                    }

                    FocusNotificationHelper.updateNotification(
                        context = getApplication(),
                        focusState = latest.currentState,
                        timerText = timerDigits,
                        activeGoalDescription = selectedGoalObj?.description,
                        isPomodoro = latest.timerMode == TimerMode.POMODORO,
                        subtitle = cycleSubtitle,
                        remainingOrElapsedSeconds = if (latest.timerMode == TimerMode.POMODORO && (latest.currentState == FocusState.Working || latest.currentState == FocusState.Break)) {
                            latest.pomodoroRemainingSeconds
                        } else {
                            latest.currentBlockElapsedSeconds
                        }
                    )
                }
            }
        }
    }

    private fun handlePomodoroPhaseCompleted() {
        val current = _uiState.value
        val now = System.currentTimeMillis()
        val currentBlockState = current.currentState
        val blockDuration = current.currentBlockElapsedSeconds + 1

        val updatedTimeline = current.currentSessionTimeline.toMutableList()
        if (blockDuration > 0 && currentBlockState != FocusState.Idle) {
            updatedTimeline.add(
                LiveBlockItem(
                    state = currentBlockState,
                    durationSeconds = blockDuration,
                    startTime = current.blockStartTime
                )
            )
        }

        if (current.pomodoroPhase == PomodoroPhase.WORK) {
            // Work interval finished -> Check whether next is long break or short break
            val isLongBreak = (current.pomodoroCurrentCycle % current.pomodoroTargetCycles) == 0
            val nextPhase = if (isLongBreak) PomodoroPhase.LONG_BREAK else PomodoroPhase.SHORT_BREAK
            val nextDurationSeconds = (if (isLongBreak) current.pomodoroLongBreakMinutes else current.pomodoroShortBreakMinutes) * 60L

            _uiState.update {
                it.copy(
                    currentState = FocusState.Break,
                    pomodoroPhase = nextPhase,
                    pomodoroRemainingSeconds = nextDurationSeconds,
                    pomodoroPhaseEndTime = now + (nextDurationSeconds * 1000L),
                    blockStartTime = now,
                    currentBlockElapsedSeconds = 0,
                    currentSessionTimeline = updatedTimeline
                )
            }
            AlarmHelper.setExactAlarm(getApplication(), now + (nextDurationSeconds * 1000L), nextPhase.name)
        } else {
            // Break finished -> Transition to Work
            val nextCycle = current.pomodoroCurrentCycle + 1
            val nextDurationSeconds = current.pomodoroWorkMinutes * 60L

            _uiState.update {
                it.copy(
                    currentState = FocusState.Working,
                    pomodoroPhase = PomodoroPhase.WORK,
                    pomodoroCurrentCycle = nextCycle,
                    pomodoroRemainingSeconds = nextDurationSeconds,
                    pomodoroPhaseEndTime = now + (nextDurationSeconds * 1000L),
                    blockStartTime = now,
                    currentBlockElapsedSeconds = 0,
                    currentSessionTimeline = updatedTimeline
                )
            }
            AlarmHelper.setExactAlarm(getApplication(), now + (nextDurationSeconds * 1000L), PomodoroPhase.WORK.name)
        }
    }

    fun setTimerMode(mode: TimerMode) {
        _uiState.update {
            val remaining = if (mode == TimerMode.POMODORO) {
                if (it.isSessionActive) {
                    it.pomodoroRemainingSeconds
                } else {
                    when (it.pomodoroPhase) {
                        PomodoroPhase.WORK -> it.pomodoroWorkMinutes * 60L
                        PomodoroPhase.SHORT_BREAK -> it.pomodoroShortBreakMinutes * 60L
                        PomodoroPhase.LONG_BREAK -> it.pomodoroLongBreakMinutes * 60L
                    }
                }
            } else {
                it.pomodoroRemainingSeconds
            }
            it.copy(timerMode = mode, pomodoroRemainingSeconds = remaining)
        }
    }

    fun setPomodoroIntervals(workMin: Int, shortBreakMin: Int, longBreakMin: Int = 15, targetCycles: Int = 4) {
        _uiState.update { current ->
            val remaining = when (current.pomodoroPhase) {
                PomodoroPhase.WORK -> workMin * 60L
                PomodoroPhase.SHORT_BREAK -> shortBreakMin * 60L
                PomodoroPhase.LONG_BREAK -> longBreakMin * 60L
            }
            current.copy(
                pomodoroWorkMinutes = workMin,
                pomodoroShortBreakMinutes = shortBreakMin,
                pomodoroLongBreakMinutes = longBreakMin,
                pomodoroTargetCycles = targetCycles,
                pomodoroRemainingSeconds = if (!current.isSessionActive) remaining else current.pomodoroRemainingSeconds
            )
        }
    }

    fun setShowPomodoroCustomDialog(show: Boolean) {
        _uiState.update { it.copy(showPomodoroCustomDialog = show) }
    }

    fun skipPomodoroPhase() {
        if (_uiState.value.isSessionActive) {
            handlePomodoroPhaseCompleted()
        } else {
            val current = _uiState.value
            if (current.pomodoroPhase == PomodoroPhase.WORK) {
                val isLongBreak = (current.pomodoroCurrentCycle % current.pomodoroTargetCycles) == 0
                val nextPhase = if (isLongBreak) PomodoroPhase.LONG_BREAK else PomodoroPhase.SHORT_BREAK
                val nextDurationSeconds = (if (isLongBreak) current.pomodoroLongBreakMinutes else current.pomodoroShortBreakMinutes) * 60L
                _uiState.update {
                    it.copy(
                        pomodoroPhase = nextPhase,
                        pomodoroRemainingSeconds = nextDurationSeconds,
                        pomodoroPhaseEndTime = System.currentTimeMillis() + (nextDurationSeconds * 1000L),
                        currentState = FocusState.Break,
                        blockStartTime = System.currentTimeMillis()
                    )
                }
                AlarmHelper.setExactAlarm(getApplication(), System.currentTimeMillis() + (nextDurationSeconds * 1000L), nextPhase.name)
            } else {
                val nextCycle = current.pomodoroCurrentCycle + 1
                val nextDurationSeconds = current.pomodoroWorkMinutes * 60L
                _uiState.update {
                    it.copy(
                        pomodoroPhase = PomodoroPhase.WORK,
                        pomodoroCurrentCycle = nextCycle,
                        pomodoroRemainingSeconds = nextDurationSeconds,
                        pomodoroPhaseEndTime = System.currentTimeMillis() + (nextDurationSeconds * 1000L),
                        currentState = FocusState.Working,
                        blockStartTime = System.currentTimeMillis()
                    )
                }
                AlarmHelper.setExactAlarm(getApplication(), System.currentTimeMillis() + (nextDurationSeconds * 1000L), PomodoroPhase.WORK.name)
            }
        }
    }

    fun resetPomodoro() {
        val current = _uiState.value
        val now = System.currentTimeMillis()
        
        val updatedTimeline = current.currentSessionTimeline.toMutableList()
        if (current.isSessionActive) {
            val blockElapsed = (now - current.blockStartTime) / 1000L
            if (blockElapsed > 0 && current.currentState != FocusState.Idle) {
                updatedTimeline.add(
                    LiveBlockItem(
                        state = current.currentState,
                        durationSeconds = blockElapsed,
                        startTime = current.blockStartTime
                    )
                )
            }
        }

        _uiState.update {
            it.copy(
                pomodoroPhase = PomodoroPhase.WORK,
                pomodoroCurrentCycle = 1,
                pomodoroRemainingSeconds = current.pomodoroWorkMinutes * 60L,
                pomodoroPhaseEndTime = now + (current.pomodoroWorkMinutes * 60L * 1000L),
                currentState = if (it.isSessionActive) FocusState.Working else it.currentState,
                blockStartTime = if (it.isSessionActive) now else it.blockStartTime,
                currentSessionTimeline = updatedTimeline
            )
        }
        if (_uiState.value.isSessionActive && _uiState.value.timerMode == TimerMode.POMODORO) {
            AlarmHelper.setExactAlarm(getApplication(), now + (current.pomodoroWorkMinutes * 60L * 1000L), PomodoroPhase.WORK.name)
        }
    }

    fun showMarkdownImportDialog(show: Boolean) {
        _uiState.update { it.copy(showMarkdownImportDialog = show) }
    }

    fun importMarkdownSession(parsed: ParsedMarkdownSession) {
        viewModelScope.launch {
            repository.saveCompletedSession(
                session = parsed.session,
                timelineBlocks = parsed.timelineBlocks,
                completedGoals = parsed.goals.filter { it.isCompleted },
                sessionDeferredTasks = parsed.deferredTasks
            )
            val pending = parsed.goals.filter { !it.isCompleted }
            for (goal in pending) {
                repository.addGoal(goal.description, goal.groupName)
            }
            showMarkdownImportDialog(false)
        }
    }

    fun startSession(initialState: FocusState = FocusState.Working) {
        val now = System.currentTimeMillis()
        _uiState.update {
            it.copy(
                currentState = initialState,
                isSessionActive = true,
                sessionStartTime = now,
                blockStartTime = now,
                currentBlockElapsedSeconds = 0,
                totalSessionElapsedSeconds = 0,
                distractionCount = 0,
                currentSessionTimeline = emptyList(),
                completedInThisSession = emptyList()
            )
        }
        val current = _uiState.value
        if (current.timerMode == TimerMode.POMODORO && current.pomodoroPhaseEndTime == 0L) {
             val nextDurationSeconds = current.pomodoroWorkMinutes * 60L
             _uiState.update {
                  it.copy(
                      pomodoroRemainingSeconds = nextDurationSeconds,
                      pomodoroPhaseEndTime = now + (nextDurationSeconds * 1000L)
                  )
             }
        }
        if (_uiState.value.timerMode == TimerMode.POMODORO) {
             AlarmHelper.setExactAlarm(getApplication(), _uiState.value.pomodoroPhaseEndTime, _uiState.value.pomodoroPhase.name)
        }
        startTimer()
    }

    fun changeState(newState: FocusState) {
        val currentState = _uiState.value.currentState
        if (currentState == newState) return

        if (!_uiState.value.isSessionActive) {
            startSession(newState)
            return
        }

        val now = System.currentTimeMillis()
        val duration = _uiState.value.currentBlockElapsedSeconds

        val updatedTimeline = _uiState.value.currentSessionTimeline.toMutableList()
        if (duration > 0 && currentState != FocusState.Idle) {
            updatedTimeline.add(
                LiveBlockItem(
                    state = currentState,
                    durationSeconds = duration,
                    startTime = _uiState.value.blockStartTime
                )
            )
        }

        _uiState.update {
            it.copy(
                currentState = newState,
                blockStartTime = now,
                currentBlockElapsedSeconds = 0,
                currentSessionTimeline = updatedTimeline
            )
        }
    }

    fun logDistraction() {
        _uiState.update {
            it.copy(distractionCount = it.distractionCount + 1)
        }
    }

    // Goal Selection & Work Tracking
    fun selectGoal(goalId: Long?) {
        _uiState.update { current ->
            val nextSelected = if (current.selectedGoalId == goalId) null else goalId
            current.copy(selectedGoalId = nextSelected)
        }
    }

    fun setSelectedGoalGroup(group: String) {
        _uiState.update { it.copy(selectedGoalGroup = group) }
    }

    fun setGoalCategoryDialog(goalId: Long?) {
        _uiState.update { it.copy(goalCategoryDialogFor = goalId) }
    }

    fun updateGoalCategory(goalId: Long, category: String) {
        viewModelScope.launch {
            val goal = _uiState.value.activeGoals.find { it.id == goalId }
            if (goal != null) {
                repository.updateGoal(goal.copy(groupName = category.trim()))
            }
        }
    }

    fun completeTopGoal() {
        val pendingGoals = _uiState.value.activeGoals.filter { !it.isCompleted }
        if (pendingGoals.isNotEmpty()) {
            val topGoal = pendingGoals.first()
            val totalElapsed = _uiState.value.totalSessionElapsedSeconds
            toggleGoalInternal(topGoal, true, totalElapsed)
        }
    }

    fun toggleGoal(goal: GoalEntity) {
        val totalElapsed = if (_uiState.value.isSessionActive) _uiState.value.totalSessionElapsedSeconds else null
        toggleGoalInternal(goal, !goal.isCompleted, totalElapsed)
    }

    private fun toggleGoalInternal(goal: GoalEntity, targetCompleted: Boolean, sessionSeconds: Long?) {
        viewModelScope.launch {
            val updated = goal.copy(
                isCompleted = targetCompleted,
                completedAtSessionSeconds = if (targetCompleted) sessionSeconds ?: 0L else null,
                completedAtTimestamp = if (targetCompleted) System.currentTimeMillis() else null
            )
            repository.updateGoal(updated)
            if (targetCompleted) {
                _uiState.update { current ->
                    val unselectIfDone = if (current.selectedGoalId == goal.id) null else current.selectedGoalId
                    current.copy(
                        completedInThisSession = current.completedInThisSession + updated,
                        selectedGoalId = unselectIfDone
                    )
                }
            } else {
                _uiState.update { current ->
                    current.copy(completedInThisSession = current.completedInThisSession.filter { it.id != goal.id })
                }
            }
        }
    }

    fun onNewGoalInputChange(text: String) {
        _uiState.update { it.copy(newGoalInput = text) }
    }

    fun addNewGoal() {
        val input = _uiState.value.newGoalInput.trim()
        val group = ""
        if (input.isNotEmpty()) {
            viewModelScope.launch {
                repository.addGoal(input, group)
                _uiState.update { it.copy(newGoalInput = "") }
            }
        }
    }

    fun deleteGoal(goalId: Long) {
        viewModelScope.launch {
            repository.deleteGoal(goalId)
            if (_uiState.value.selectedGoalId == goalId) {
                _uiState.update { it.copy(selectedGoalId = null) }
            }
        }
    }

    // Mind Dump Options & Grouping
    fun toggleHideMindDumps() {
        _uiState.update { it.copy(hideMindDumps = !it.hideMindDumps) }
    }

    fun toggleZenMode() {
        _uiState.update { it.copy(isZenModeEnabled = !it.isZenModeEnabled) }
    }

    fun setHideMindDumps(hide: Boolean) {
        _uiState.update { it.copy(hideMindDumps = hide) }
    }

    fun setSelectedMindDumpGroup(group: String) {
        _uiState.update { it.copy(selectedMindDumpGroup = group) }
    }

    fun setMindDumpCategoryDialog(taskId: Long?) {
        _uiState.update { it.copy(mindDumpCategoryDialogFor = taskId) }
    }

    fun updateMindDumpCategory(taskId: Long, category: String) {
        viewModelScope.launch {
            val task = _uiState.value.deferredTasks.find { it.id == taskId }
            if (task != null) {
                repository.updateDeferredTask(task.copy(groupName = category.trim()))
            }
        }
    }

    fun onQuickDumpTextChange(text: String) {
        _uiState.update { it.copy(quickDumpText = text) }
    }

    fun addQuickDumpTask() {
        val text = _uiState.value.quickDumpText.trim()
        val group = ""
        if (text.isNotEmpty()) {
            viewModelScope.launch {
                repository.addDeferredTask(text, group)
                logDistraction() // Automatically increment mind wander count
                _uiState.update { it.copy(quickDumpText = "") }
            }
        }
    }

    fun toggleDeferredTask(task: DeferredTaskEntity) {
        viewModelScope.launch {
            repository.updateDeferredTask(task.copy(isCompleted = !task.isCompleted))
        }
    }

    fun deleteDeferredTask(taskId: Long) {
        viewModelScope.launch {
            repository.deleteDeferredTask(taskId)
        }
    }

    fun endSession() {
        if (!_uiState.value.isSessionActive) return

        val now = System.currentTimeMillis()
        val currentBlock = _uiState.value.currentState
        val currentDuration = _uiState.value.currentBlockElapsedSeconds

        // Push final block to timeline
        val timelineList = _uiState.value.currentSessionTimeline.toMutableList()
        if (currentDuration > 0 && currentBlock != FocusState.Idle) {
            timelineList.add(
                LiveBlockItem(
                    state = currentBlock,
                    durationSeconds = currentDuration,
                    startTime = _uiState.value.blockStartTime
                )
            )
        }

        // Calculate totals
        var totalWork = 0L
        var totalBreak = 0L
        var totalSlack = 0L
        for (item in timelineList) {
            when (item.state) {
                FocusState.Working -> totalWork += item.durationSeconds
                FocusState.Break -> totalBreak += item.durationSeconds
                FocusState.Procrastination -> totalSlack += item.durationSeconds
                else -> {}
            }
        }

        val completedGoals = _uiState.value.activeGoals.filter { it.isCompleted }
        val pendingGoals = _uiState.value.activeGoals.filter { !it.isCompleted }
        val sessionDeferredTasks = _uiState.value.deferredTasks.filter {
            it.createdAtTimestamp >= _uiState.value.sessionStartTime
        }

        val sessionEntity = FocusSessionEntity(
            startTime = _uiState.value.sessionStartTime,
            endTime = now,
            totalWorkSeconds = totalWork,
            totalBreakSeconds = totalBreak,
            totalSlackSeconds = totalSlack,
            distractionCount = _uiState.value.distractionCount,
            completedGoalCount = completedGoals.size,
            pendingGoalCount = pendingGoals.size
        )

        val blockEntities = timelineList.mapIndexed { index, live ->
            TimelineBlockEntity(
                sessionId = 0,
                state = live.state.name,
                startTime = live.startTime,
                durationSeconds = live.durationSeconds,
                orderIndex = index,
                relatedGoalId = live.relatedGoalId
            )
        }

        val markdown = MarkdownExporter.generateMarkdown(
            session = sessionEntity,
            timelineBlocks = blockEntities,
            goals = _uiState.value.activeGoals,
            deferredTasks = sessionDeferredTasks
        )

        viewModelScope.launch {
            // Save active goals updated spent seconds to DB
            for (goal in _uiState.value.activeGoals) {
                repository.updateGoal(goal)
            }

            val sessionId = repository.saveCompletedSession(
                session = sessionEntity,
                timelineBlocks = blockEntities,
                completedGoals = completedGoals,
                sessionDeferredTasks = sessionDeferredTasks
            )

            val finishedDetails = SessionWithDetails(
                session = sessionEntity.copy(id = sessionId),
                timelineBlocks = blockEntities.map { it.copy(sessionId = sessionId) },
                goals = _uiState.value.activeGoals,
                deferredTasks = sessionDeferredTasks
            )

            _uiState.update {
                it.copy(
                    currentState = FocusState.Idle,
                    isSessionActive = false,
                    currentBlockElapsedSeconds = 0,
                    totalSessionElapsedSeconds = 0,
                    justFinishedSession = finishedDetails,
                    justFinishedMarkdown = markdown
                )
            }
        }

        timerJob?.cancel()
        FocusNotificationHelper.cancelNotification(getApplication())
        AlarmHelper.cancelAlarm(getApplication())
    }

    fun dismissFinishedSessionDialog() {
        _uiState.update {
            it.copy(
                justFinishedSession = null,
                justFinishedMarkdown = null
            )
        }
    }

    fun selectSessionForDetail(session: SessionWithDetails?) {
        _uiState.update { it.copy(selectedSessionForDetail = session) }
    }

    fun deleteSession(sessionId: Long) {
        viewModelScope.launch {
            repository.deleteSession(sessionId)
            if (_uiState.value.selectedSessionForDetail?.session?.id == sessionId) {
                _uiState.update { it.copy(selectedSessionForDetail = null) }
            }
        }
    }

    fun setThemeMode(themeMode: ThemeMode) {
        prefs.edit().putString("theme_mode", themeMode.name).apply()
        _uiState.update { it.copy(themeMode = themeMode) }
    }

    fun setThemePreset(themePreset: ThemePreset) {
        prefs.edit().putString("theme_preset", themePreset.name).apply()
        _uiState.update { it.copy(themePreset = themePreset) }
    }

    fun toggleNotificationPanel() {
        _uiState.update { it.copy(showNotificationPanel = !it.showNotificationPanel) }
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAllData()
        }
    }


    fun setAnalyticsPeriod(period: AnalyticsPeriod) {
        _analyticsPeriod.value = period
        _analyticsOffset.value = 0
    }

    fun shiftAnalyticsOffset(delta: Int) {
        _analyticsOffset.value += delta
    }

    private fun filterSessionsByPeriod(
        sessions: List<com.example.data.entity.SessionWithDetails>,
        period: AnalyticsPeriod,
        offset: Int
    ): List<com.example.data.entity.SessionWithDetails> {
        val cal = java.util.Calendar.getInstance()
        
        when (period) {
            AnalyticsPeriod.WEEK -> {
                // start of week
                cal.set(java.util.Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
                cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
                cal.set(java.util.Calendar.MINUTE, 0)
                cal.set(java.util.Calendar.SECOND, 0)
                cal.set(java.util.Calendar.MILLISECOND, 0)
                cal.add(java.util.Calendar.WEEK_OF_YEAR, offset)
                val start = cal.timeInMillis
                cal.add(java.util.Calendar.WEEK_OF_YEAR, 1)
                val end = cal.timeInMillis
                return sessions.filter { it.session.startTime in start until end }
            }
            AnalyticsPeriod.MONTH -> {
                cal.set(java.util.Calendar.DAY_OF_MONTH, 1)
                cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
                cal.set(java.util.Calendar.MINUTE, 0)
                cal.set(java.util.Calendar.SECOND, 0)
                cal.set(java.util.Calendar.MILLISECOND, 0)
                cal.add(java.util.Calendar.MONTH, offset)
                cal.add(java.util.Calendar.MONTH, 1)
                val end = cal.timeInMillis
                cal.add(java.util.Calendar.MONTH, -4)
                val start = cal.timeInMillis
                return sessions.filter { it.session.startTime in start until end }
            }
        }
    }

    private fun calculateAnalytics(
        sessions: List<SessionWithDetails>,
        period: AnalyticsPeriod = AnalyticsPeriod.WEEK,
        offset: Int = 0
    ): OverallAnalytics {
        if (sessions.isEmpty()) return OverallAnalytics()

        var totalWork = 0L
        var totalBreak = 0L
        var totalSlack = 0L
        var totalDistractions = 0
        var totalGoals = 0

        val groupedByDate = LinkedHashMap<String, MutableList<SessionWithDetails>>()
        val keyFormat = if (period == com.example.ui.viewmodel.AnalyticsPeriod.WEEK) {
            java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        } else {
            java.text.SimpleDateFormat("yyyy-MM", java.util.Locale.getDefault())
        }
        
        val calEmpty = java.util.Calendar.getInstance()
        if (period == com.example.ui.viewmodel.AnalyticsPeriod.WEEK) {
            calEmpty.set(java.util.Calendar.DAY_OF_WEEK, calEmpty.firstDayOfWeek)
            calEmpty.add(java.util.Calendar.WEEK_OF_YEAR, offset)
            for (i in 0..6) {
                groupedByDate[keyFormat.format(calEmpty.time)] = mutableListOf()
                calEmpty.add(java.util.Calendar.DAY_OF_YEAR, 1)
            }
        } else {
            calEmpty.set(java.util.Calendar.DAY_OF_MONTH, 1)
            calEmpty.add(java.util.Calendar.MONTH, offset - 3)
            for (i in 0..3) {
                groupedByDate[keyFormat.format(calEmpty.time)] = mutableListOf()
                calEmpty.add(java.util.Calendar.MONTH, 1)
            }
        }

        val overallTimePerTask = mutableMapOf<String, Long>()
        for (item in sessions) {
            val s = item.session
            totalWork += s.totalWorkSeconds
            totalBreak += s.totalBreakSeconds
            totalSlack += s.totalSlackSeconds
            totalDistractions += s.distractionCount
            totalGoals += s.completedGoalCount
            
            val goalMap = item.goals.associateBy { it.id }
            for (block in item.timelineBlocks) {
                if (block.state == FocusState.Working.name && block.relatedGoalId != null) {
                    val goalName = goalMap[block.relatedGoalId]?.description ?: "Unknown Task"
                    overallTimePerTask[goalName] = overallTimePerTask.getOrDefault(goalName, 0L) + block.durationSeconds
                }
            }

            val dateKey = keyFormat.format(java.util.Date(s.startTime))
            if (!groupedByDate.containsKey(dateKey)) {
                groupedByDate[dateKey] = mutableListOf()
            }
            groupedByDate[dateKey]?.add(item)
        }

        val dailySummaries = groupedByDate.map { (dateKey, list) ->
            var dWork = 0L
            var dBreak = 0L
            var dSlack = 0L
            var dDistractions = 0
            var dGoals = 0

            val dailyTimePerTask = mutableMapOf<String, Long>()
            for (sessionWithDetails in list) {
                val s = sessionWithDetails.session
                dWork += s.totalWorkSeconds
                dBreak += s.totalBreakSeconds
                dSlack += s.totalSlackSeconds
                dDistractions += s.distractionCount
                dGoals += s.completedGoalCount
                
                val goalMap = sessionWithDetails.goals.associateBy { it.id }
                for (block in sessionWithDetails.timelineBlocks) {
                    if (block.state == FocusState.Working.name && block.relatedGoalId != null) {
                        val goalName = goalMap[block.relatedGoalId]?.description ?: "Unknown Task"
                        dailyTimePerTask[goalName] = dailyTimePerTask.getOrDefault(goalName, 0L) + block.durationSeconds
                    }
                }
            }

            val parsedDate = keyFormat.parse(dateKey) ?: java.util.Date()
            val displayFmt = if (period == com.example.ui.viewmodel.AnalyticsPeriod.WEEK) {
                java.text.SimpleDateFormat("MMM d", java.util.Locale.getDefault())
            } else {
                java.text.SimpleDateFormat("MMM", java.util.Locale.getDefault())
            }
            DailySummary(
                dateString = dateKey,
                displayDate = displayFmt.format(parsedDate),
                totalWorkSeconds = dWork,
                totalBreakSeconds = dBreak,
                totalSlackSeconds = dSlack,
                totalDistractions = dDistractions,
                sessionCount = list.size,
                completedGoalsCount = dGoals,
                sessions = list,
                timePerTask = dailyTimePerTask
            )
        }

        val totalDuration = totalWork + totalBreak + totalSlack
        val overallEfficiency = if (totalDuration > 0) (totalWork.toFloat() / totalDuration.toFloat()) * 100f else 0f
        val avgSeconds = if (sessions.isNotEmpty()) totalDuration / sessions.size else 0L

        var currentStreak = 0
        val cal = java.util.Calendar.getInstance()
        var dateToCheck = TimeFormatter.formatDateOnly(cal.timeInMillis)

        if (groupedByDate.containsKey(dateToCheck)) {
            currentStreak++
            cal.add(java.util.Calendar.DAY_OF_YEAR, -1)
            dateToCheck = TimeFormatter.formatDateOnly(cal.timeInMillis)
            while (groupedByDate.containsKey(dateToCheck)) {
                currentStreak++
                cal.add(java.util.Calendar.DAY_OF_YEAR, -1)
                dateToCheck = TimeFormatter.formatDateOnly(cal.timeInMillis)
            }
        } else {
            cal.add(java.util.Calendar.DAY_OF_YEAR, -1)
            dateToCheck = TimeFormatter.formatDateOnly(cal.timeInMillis)
            while (groupedByDate.containsKey(dateToCheck)) {
                currentStreak++
                cal.add(java.util.Calendar.DAY_OF_YEAR, -1)
                dateToCheck = TimeFormatter.formatDateOnly(cal.timeInMillis)
            }
        }

        return OverallAnalytics(
            totalWorkSeconds = totalWork,
            totalBreakSeconds = totalBreak,
            totalSlackSeconds = totalSlack,
            totalDistractions = totalDistractions,
            totalSessions = sessions.size,
            totalGoalsCompleted = totalGoals,
            averageSessionSeconds = avgSeconds,
            overallEfficiency = overallEfficiency,
            currentStreak = currentStreak,
            dailySummaries = dailySummaries,
            overallTimePerTask = overallTimePerTask
        )
    }

    fun populateSampleData() {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val dayMs = 24 * 3600 * 1000L

            repository.addGoal("Implement core focus state machine", "Coding")
            repository.addGoal("Translate C++ timeline exporter to Markdown", "Coding")
            repository.addGoal("Design responsive tablet & mobile layout", "Design")
            repository.addGoal("Add day-by-day analysis & local Room persistence", "Study")

            val pastDates = listOf(now - 2 * dayMs, now - dayMs, now - 3600 * 1000L)
            for ((idx, startTime) in pastDates.withIndex()) {
                val workSec = (25 + idx * 15) * 60L
                val breakSec = (5 + idx * 3) * 60L
                val slackSec = (3 + idx * 2) * 60L
                val endTime = startTime + (workSec + breakSec + slackSec) * 1000L

                val sampleSession = FocusSessionEntity(
                    startTime = startTime,
                    endTime = endTime,
                    totalWorkSeconds = workSec,
                    totalBreakSeconds = breakSec,
                    totalSlackSeconds = slackSec,
                    distractionCount = 1 + idx,
                    completedGoalCount = 1 + idx,
                    pendingGoalCount = 2
                )

                val blocks = listOf(
                    TimelineBlockEntity(sessionId = 0, state = FocusState.Working.name, startTime = startTime, durationSeconds = workSec / 2, orderIndex = 0, relatedGoalId = 1L),
                    TimelineBlockEntity(sessionId = 0, state = FocusState.Break.name, startTime = startTime + (workSec / 2) * 1000L, durationSeconds = breakSec, orderIndex = 1),
                    TimelineBlockEntity(sessionId = 0, state = FocusState.Working.name, startTime = startTime + (workSec / 2 + breakSec) * 1000L, durationSeconds = workSec / 2, orderIndex = 2, relatedGoalId = 1L),
                    TimelineBlockEntity(sessionId = 0, state = FocusState.Procrastination.name, startTime = startTime + (workSec + breakSec) * 1000L, durationSeconds = slackSec, orderIndex = 3)
                )

                val sampleGoals = listOf(
                    GoalEntity(
                        description = "Refactor session struct to Kotlin Entity #$idx",
                        isCompleted = true,
                        completedAtSessionSeconds = workSec / 2,
                        groupName = "Coding",
                        totalWorkSecondsSpent = workSec / 2
                    )
                )

                val sampleTasks = listOf(
                    DeferredTaskEntity(text = "Check pull request comments #$idx", isCompleted = idx % 2 == 0, groupName = "Work")
                )

                repository.saveCompletedSession(sampleSession, blocks, sampleGoals, sampleTasks)
            }
        }
    }
}

data class AnalyticsData(val sessions: List<com.example.data.entity.SessionWithDetails>, val period: AnalyticsPeriod, val offset: Int)
