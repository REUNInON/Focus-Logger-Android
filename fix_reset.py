import re

with open('app/src/main/java/com/example/ui/viewmodel/FocusViewModel.kt', 'r') as f:
    content = f.read()

old_reset = """    fun resetPomodoro() {
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

new_reset = """    fun resetPomodoro() {
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
    }"""

content = content.replace(old_reset, new_reset)

with open('app/src/main/java/com/example/ui/viewmodel/FocusViewModel.kt', 'w') as f:
    f.write(content)
