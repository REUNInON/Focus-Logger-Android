import re

with open('app/src/main/java/com/example/ui/components/TimerNotificationPanel.kt', 'r') as f:
    content = f.read()

# 1. Update the top line left side to handle weights so it doesn't overflow
old_left = """                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(animatedColor)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (timerMode == TimerMode.POMODORO) pomodoroPhase.displayName.uppercase() else currentState.displayName.uppercase(),
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            color = animatedColor
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (timerMode == TimerMode.POMODORO) {
                                TimeFormatter.formatDuration(pomodoroRemainingSeconds)
                            } else {
                                TimeFormatter.formatDuration(blockElapsedSeconds)
                            },
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }"""

new_left = """                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f).padding(end = 8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(animatedColor)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (timerMode == TimerMode.POMODORO) pomodoroPhase.displayName.uppercase() else currentState.displayName.uppercase(),
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            ),
                            color = animatedColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (timerMode == TimerMode.POMODORO) {
                                TimeFormatter.formatDuration(pomodoroRemainingSeconds)
                            } else {
                                TimeFormatter.formatDuration(blockElapsedSeconds)
                            },
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }"""

content = content.replace(old_left, new_left)

# 2. Hide Wander in panel when not working
old_wander = """                        // +1 Wander
                        Surface(
                            onClick = onLogDistraction,
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFF59E0B).copy(alpha = 0.2f),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Psychology,
                                    contentDescription = null,
                                    tint = Color(0xFFF59E0B),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = "+1 ($distractionCount)",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFFF59E0B)
                                )
                            }
                        }"""

new_wander = """                        // +1 Wander
                        androidx.compose.animation.AnimatedVisibility(visible = currentState == FocusState.Working) {
                            Surface(
                                onClick = onLogDistraction,
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFF59E0B).copy(alpha = 0.2f),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Psychology,
                                        contentDescription = null,
                                        tint = Color(0xFFF59E0B),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = "+1 ($distractionCount)",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = Color(0xFFF59E0B)
                                    )
                                }
                            }
                        }"""

content = content.replace(old_wander, new_wander)

with open('app/src/main/java/com/example/ui/components/TimerNotificationPanel.kt', 'w') as f:
    f.write(content)
