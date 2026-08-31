import re

with open('./app/src/main/java/com/example/ui/viewmodel/FocusViewModel.kt', 'r') as f:
    content = f.read()

# Replace the pomodoro logic inside startTimer()
old_logic = """                    if (current.timerMode == TimerMode.POMODORO) {
                        val newRemaining = current.pomodoroRemainingSeconds - 1
                        if (newRemaining <= 0) {
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
                    }"""

new_logic = """                    if (current.timerMode == TimerMode.POMODORO) {
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
                    }"""

content = content.replace(old_logic, new_logic)

with open('./app/src/main/java/com/example/ui/viewmodel/FocusViewModel.kt', 'w') as f:
    f.write(content)
