import re

with open('app/src/main/java/com/example/domain/util/FocusNotificationHelper.kt', 'r') as f:
    content = f.read()

old_fun = """    fun updateNotification(
        context: Context,
        focusState: FocusState,
        timerText: String,
        activeGoalDescription: String?,
        isPomodoro: Boolean,
        subtitle: String
    ) {"""

new_fun = """    fun updateNotification(
        context: Context,
        focusState: FocusState,
        timerText: String,
        activeGoalDescription: String?,
        isPomodoro: Boolean,
        subtitle: String,
        remainingOrElapsedSeconds: Long
    ) {"""

content = content.replace(old_fun, new_fun)

builder_old = """                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)"""

builder_new = """                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setUsesChronometer(true)
            
            if (isPomodoro && (focusState == FocusState.Working || focusState == FocusState.Break)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    builder.setChronometerCountDown(true)
                }
                builder.setWhen(System.currentTimeMillis() + (remainingOrElapsedSeconds * 1000L))
            } else {
                builder.setWhen(System.currentTimeMillis() - (remainingOrElapsedSeconds * 1000L))
            }"""

content = content.replace(builder_old, builder_new)

with open('app/src/main/java/com/example/domain/util/FocusNotificationHelper.kt', 'w') as f:
    f.write(content)
