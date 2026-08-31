import re

with open('app/src/main/java/com/example/ui/viewmodel/FocusViewModel.kt', 'r') as f:
    content = f.read()

# 1. Fix endSession() to reset pomodoro states completely
old_end = """            _uiState.update {
                it.copy(
                    isSessionActive = false,
                    currentState = FocusState.Idle,
                    sessionStartTime = 0L,
                    blockStartTime = 0L,
                    currentBlockElapsedSeconds = 0,
                    totalSessionElapsedSeconds = 0,
                    distractionCount = 0,
                    currentSessionTimeline = emptyList(),
                    activeGoals = goals,
                    showNotificationPanel = false
                )
            }"""

new_end = """            _uiState.update {
                val resetRemaining = it.pomodoroWorkMinutes * 60L
                it.copy(
                    isSessionActive = false,
                    currentState = FocusState.Idle,
                    sessionStartTime = 0L,
                    blockStartTime = 0L,
                    currentBlockElapsedSeconds = 0,
                    totalSessionElapsedSeconds = 0,
                    distractionCount = 0,
                    currentSessionTimeline = emptyList(),
                    activeGoals = goals,
                    showNotificationPanel = false,
                    pomodoroPhase = PomodoroPhase.WORK,
                    pomodoroCurrentCycle = 1,
                    pomodoroRemainingSeconds = resetRemaining,
                    pomodoroPhaseEndTime = 0L
                )
            }"""

content = content.replace(old_end, new_end)

# 2. Fix resetPomodoro() to also reset the currentState and blockStartTime if the session is active
old_reset = """    fun resetPomodoro() {
        val current = _uiState.value
        _uiState.update {
            it.copy(
                pomodoroPhase = PomodoroPhase.WORK,
                pomodoroCurrentCycle = 1,
                pomodoroRemainingSeconds = current.pomodoroWorkMinutes * 60L,
                pomodoroPhaseEndTime = System.currentTimeMillis() + (current.pomodoroWorkMinutes * 60L * 1000L)
            )
        }
        if (_uiState.value.isSessionActive && _uiState.value.timerMode == TimerMode.POMODORO) {
            AlarmHelper.setExactAlarm(getApplication(), System.currentTimeMillis() + (current.pomodoroWorkMinutes * 60L * 1000L), PomodoroPhase.WORK.name)
        }
    }"""

new_reset = """    fun resetPomodoro() {
        val current = _uiState.value
        val now = System.currentTimeMillis()
        _uiState.update {
            it.copy(
                pomodoroPhase = PomodoroPhase.WORK,
                pomodoroCurrentCycle = 1,
                pomodoroRemainingSeconds = current.pomodoroWorkMinutes * 60L,
                pomodoroPhaseEndTime = now + (current.pomodoroWorkMinutes * 60L * 1000L),
                currentState = if (it.isSessionActive) FocusState.Working else it.currentState,
                blockStartTime = if (it.isSessionActive) now else it.blockStartTime
            )
        }
        if (_uiState.value.isSessionActive && _uiState.value.timerMode == TimerMode.POMODORO) {
            AlarmHelper.setExactAlarm(getApplication(), now + (current.pomodoroWorkMinutes * 60L * 1000L), PomodoroPhase.WORK.name)
        }
    }"""

content = content.replace(old_reset, new_reset)

with open('app/src/main/java/com/example/ui/viewmodel/FocusViewModel.kt', 'w') as f:
    f.write(content)
