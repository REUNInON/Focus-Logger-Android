with open('app/src/main/java/com/example/ui/theme/Theme.kt', 'r') as f:
    content = f.read()

content = content.replace("VERSION.CODES.S", "VERSION_CODES.S")

with open('app/src/main/java/com/example/ui/theme/Theme.kt', 'w') as f:
    f.write(content)

with open('app/src/main/java/com/example/ui/viewmodel/FocusViewModel.kt', 'r') as f:
    content = f.read()

content = content.replace("goalMap[block.relatedGoalId]?.title", "goalMap[block.relatedGoalId]?.description")

old_call = """                    FocusNotificationHelper.updateNotification(
                        context = getApplication(),
                        focusState = latest.currentState,
                        timerText = timerDigits,
                        activeGoalDescription = selectedGoalObj?.description,
                        isPomodoro = latest.timerMode == TimerMode.POMODORO,
                        subtitle = cycleSubtitle
                    )"""

new_call = """                    FocusNotificationHelper.updateNotification(
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
                    )"""

content = content.replace(old_call, new_call)

with open('app/src/main/java/com/example/ui/viewmodel/FocusViewModel.kt', 'w') as f:
    f.write(content)
