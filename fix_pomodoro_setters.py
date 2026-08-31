import re

with open('./app/src/main/java/com/example/ui/viewmodel/FocusViewModel.kt', 'r') as f:
    content = f.read()

def replace_phase_completed():
    global content
    old = """            _uiState.update {
                it.copy(
                    currentState = FocusState.Break,
                    pomodoroPhase = nextPhase,
                    pomodoroRemainingSeconds = nextDurationSeconds,
                    blockStartTime = now,
                    currentBlockElapsedSeconds = 0,
                    currentSessionTimeline = updatedTimeline
                )
            }"""
    new = """            _uiState.update {
                it.copy(
                    currentState = FocusState.Break,
                    pomodoroPhase = nextPhase,
                    pomodoroRemainingSeconds = nextDurationSeconds,
                    pomodoroPhaseEndTime = now + (nextDurationSeconds * 1000L),
                    blockStartTime = now,
                    currentBlockElapsedSeconds = 0,
                    currentSessionTimeline = updatedTimeline
                )
            }"""
    content = content.replace(old, new)
    
    old2 = """            _uiState.update {
                it.copy(
                    currentState = FocusState.Working,
                    pomodoroPhase = PomodoroPhase.WORK,
                    pomodoroCurrentCycle = nextCycle,
                    pomodoroRemainingSeconds = nextDurationSeconds,
                    blockStartTime = now,
                    currentBlockElapsedSeconds = 0,
                    currentSessionTimeline = updatedTimeline
                )
            }"""
    new2 = """            _uiState.update {
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
            }"""
    content = content.replace(old2, new2)

def replace_start_session():
    global content
    old = """        _uiState.update {
            it.copy(
                isSessionActive = true,
                currentState = initialState,
                sessionStartTime = now,
                blockStartTime = now,
                currentBlockElapsedSeconds = 0,
                totalSessionElapsedSeconds = 0,
                distractionCount = 0,
                currentSessionTimeline = emptyList(),
                completedInThisSession = emptyList()
            )
        }"""
    new = """        _uiState.update {
            val endTime = if (it.timerMode == TimerMode.POMODORO) {
                now + (it.pomodoroRemainingSeconds * 1000L)
            } else 0L
            it.copy(
                isSessionActive = true,
                currentState = initialState,
                sessionStartTime = now,
                blockStartTime = now,
                currentBlockElapsedSeconds = 0,
                totalSessionElapsedSeconds = 0,
                distractionCount = 0,
                currentSessionTimeline = emptyList(),
                completedInThisSession = emptyList(),
                pomodoroPhaseEndTime = endTime
            )
        }"""
    content = content.replace(old, new)

def replace_skip():
    global content
    old = """                _uiState.update {
                    it.copy(
                        pomodoroPhase = nextPhase,
                        pomodoroRemainingSeconds = nextDurationSeconds,
                        currentState = FocusState.Break
                    )
                }"""
    new = """                _uiState.update {
                    it.copy(
                        pomodoroPhase = nextPhase,
                        pomodoroRemainingSeconds = nextDurationSeconds,
                        pomodoroPhaseEndTime = System.currentTimeMillis() + (nextDurationSeconds * 1000L),
                        currentState = FocusState.Break,
                        blockStartTime = System.currentTimeMillis()
                    )
                }"""
    content = content.replace(old, new)
    
    old2 = """                _uiState.update {
                    it.copy(
                        pomodoroPhase = PomodoroPhase.WORK,
                        pomodoroCurrentCycle = nextCycle,
                        pomodoroRemainingSeconds = nextDurationSeconds,
                        currentState = FocusState.Working
                    )
                }"""
    new2 = """                _uiState.update {
                    it.copy(
                        pomodoroPhase = PomodoroPhase.WORK,
                        pomodoroCurrentCycle = nextCycle,
                        pomodoroRemainingSeconds = nextDurationSeconds,
                        pomodoroPhaseEndTime = System.currentTimeMillis() + (nextDurationSeconds * 1000L),
                        currentState = FocusState.Working,
                        blockStartTime = System.currentTimeMillis()
                    )
                }"""
    content = content.replace(old2, new2)

def replace_reset():
    global content
    old = """        _uiState.update {
            it.copy(
                pomodoroPhase = PomodoroPhase.WORK,
                pomodoroCurrentCycle = 1,
                pomodoroRemainingSeconds = current.pomodoroWorkMinutes * 60L
            )
        }"""
    new = """        _uiState.update {
            it.copy(
                pomodoroPhase = PomodoroPhase.WORK,
                pomodoroCurrentCycle = 1,
                pomodoroRemainingSeconds = current.pomodoroWorkMinutes * 60L,
                pomodoroPhaseEndTime = System.currentTimeMillis() + (current.pomodoroWorkMinutes * 60L * 1000L)
            )
        }"""
    content = content.replace(old, new)

replace_phase_completed()
replace_start_session()
replace_skip()
replace_reset()

with open('./app/src/main/java/com/example/ui/viewmodel/FocusViewModel.kt', 'w') as f:
    f.write(content)
