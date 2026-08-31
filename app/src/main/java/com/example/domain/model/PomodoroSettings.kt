package com.example.domain.model

enum class TimerMode(val displayName: String) {
    STOPWATCH("Stopwatch"),
    POMODORO("Pomodoro")
}

enum class PomodoroPhase(
    val displayName: String,
    val focusState: FocusState,
    val defaultMinutes: Int
) {
    WORK("Focus Work", FocusState.Working, 25),
    SHORT_BREAK("Short Break", FocusState.Break, 5),
    LONG_BREAK("Long Break", FocusState.Break, 15)
}
