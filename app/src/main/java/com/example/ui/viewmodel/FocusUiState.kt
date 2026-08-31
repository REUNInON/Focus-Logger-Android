package com.example.ui.viewmodel

import com.example.data.entity.DeferredTaskEntity
import com.example.data.entity.FocusSessionEntity
import com.example.data.entity.GoalEntity
import com.example.data.entity.SessionWithDetails
import com.example.data.entity.TimelineBlockEntity
import com.example.domain.model.DailySummary
import com.example.domain.model.FocusState
import com.example.domain.model.color
import com.example.domain.model.OverallAnalytics
import com.example.domain.model.PomodoroPhase
import com.example.domain.model.ThemeMode
import com.example.domain.model.ThemePreset
import com.example.domain.model.TimerMode

enum class AnalyticsPeriod { WEEK, MONTH }

data class LiveBlockItem(
    val state: FocusState,
    val durationSeconds: Long,
    val startTime: Long,
    val relatedGoalId: Long? = null
)

data class FocusUiState(
    val currentState: FocusState = FocusState.Idle,
    val isSessionActive: Boolean = false,
    val currentBlockElapsedSeconds: Long = 0,
    val totalSessionElapsedSeconds: Long = 0,
    val sessionStartTime: Long = 0,
    val blockStartTime: Long = 0,
    val distractionCount: Int = 0,
    val currentSessionTimeline: List<LiveBlockItem> = emptyList(),

    // Pomodoro Mode State
    val timerMode: TimerMode = TimerMode.STOPWATCH,
    val pomodoroPhase: PomodoroPhase = PomodoroPhase.WORK,
    val pomodoroWorkMinutes: Int = 25,
    val pomodoroShortBreakMinutes: Int = 5,
    val pomodoroLongBreakMinutes: Int = 15,
    val pomodoroTargetCycles: Int = 4,
    val pomodoroCurrentCycle: Int = 1,
    val pomodoroRemainingSeconds: Long = 25 * 60L,
    val pomodoroPhaseEndTime: Long = 0,
    val pomodoroAutoStartNext: Boolean = false,

    // Goals (Carried over from DB & Selection tracking)
    val activeGoals: List<GoalEntity> = emptyList(),
    val completedInThisSession: List<GoalEntity> = emptyList(),
    val selectedGoalId: Long? = null,
    val selectedGoalGroup: String = "All",
    val availableGoalGroups: List<String> = listOf(),
    val goalCategoryDialogFor: Long? = null,

    // Mind dump / Deferred tasks
    val deferredTasks: List<DeferredTaskEntity> = emptyList(),
    val quickDumpText: String = "",
    val newGoalInput: String = "",
    val hideMindDumps: Boolean = false,
    val isZenModeEnabled: Boolean = false,
    val selectedMindDumpGroup: String = "All",
    val availableMindDumpGroups: List<String> = listOf(),
    val mindDumpCategoryDialogFor: Long? = null,
    
    // Pomodoro Customization
    val showPomodoroCustomDialog: Boolean = false,

    // Notification Panel
    val showNotificationPanel: Boolean = true,

    // History & Analytics
    val pastSessions: List<SessionWithDetails> = emptyList(),
    val overallAnalytics: OverallAnalytics = OverallAnalytics(),
    val analyticsPeriod: AnalyticsPeriod = AnalyticsPeriod.WEEK,
    val analyticsOffset: Int = 0,
    val selectedSessionForDetail: SessionWithDetails? = null,

    // End session summary dialog
    val justFinishedSession: SessionWithDetails? = null,
    val justFinishedMarkdown: String? = null,

    // Import Dialog
    val showMarkdownImportDialog: Boolean = false,

    // Settings & Theming (12+ themes)
    val themePreset: ThemePreset = ThemePreset.MIDNIGHT_OBSIDIAN,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val soundHapticsEnabled: Boolean = true
)
