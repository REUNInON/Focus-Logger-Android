import re

with open('app/src/main/java/com/example/ui/viewmodel/FocusViewModel.kt', 'r') as f:
    content = f.read()

# Find the call to updateNotification and replace it.
old_call = """                FocusNotificationHelper.updateNotification(
                    context = getApplication(),
                    focusState = _uiState.value.currentState,
                    timerText = timeStr,
                    activeGoalDescription = _uiState.value.activeGoals.find { it.id == _uiState.value.selectedGoalId }?.title,
                    isPomodoro = _uiState.value.timerMode == TimerMode.POMODORO,
                    subtitle = subtitle
                )"""

new_call = """                FocusNotificationHelper.updateNotification(
                    context = getApplication(),
                    focusState = _uiState.value.currentState,
                    timerText = timeStr,
                    activeGoalDescription = _uiState.value.activeGoals.find { it.id == _uiState.value.selectedGoalId }?.title,
                    isPomodoro = _uiState.value.timerMode == TimerMode.POMODORO,
                    subtitle = subtitle,
                    remainingOrElapsedSeconds = if (_uiState.value.timerMode == TimerMode.POMODORO && (_uiState.value.currentState == FocusState.Working || _uiState.value.currentState == FocusState.Break)) {
                        _uiState.value.pomodoroRemainingSeconds
                    } else {
                        _uiState.value.totalSessionElapsedSeconds
                    }
                )"""

content = content.replace(old_call, new_call)

with open('app/src/main/java/com/example/ui/viewmodel/FocusViewModel.kt', 'w') as f:
    f.write(content)
