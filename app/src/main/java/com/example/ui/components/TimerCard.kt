package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.FocusState
import com.example.domain.model.color
import com.example.domain.model.PomodoroPhase
import com.example.domain.model.TimerMode
import com.example.domain.util.TimeFormatter

@Composable
fun TimerCard(
    currentState: FocusState,
    isSessionActive: Boolean,
    blockElapsedSeconds: Long,
    totalSessionElapsedSeconds: Long,
    distractionCount: Int,
    timerMode: TimerMode,
    pomodoroPhase: PomodoroPhase,
    pomodoroRemainingSeconds: Long,
    pomodoroCurrentCycle: Int,
    pomodoroTargetCycles: Int,
    pomodoroWorkMinutes: Int,
    pomodoroShortBreakMinutes: Int,
    onTimerModeChange: (TimerMode) -> Unit,
    onPomodoroPresetSelect: (Int, Int) -> Unit,
    onCustomPomodoroClick: () -> Unit,
    onSkipPomodoroPhase: () -> Unit,
    onResetPomodoro: () -> Unit,
    onStateChange: (FocusState) -> Unit,
    onStartSession: (FocusState) -> Unit,
    onEndSession: () -> Unit,
    onLogDistraction: () -> Unit,
    modifier: Modifier = Modifier
) {
    val stateColor = currentState.color()
    val animatedStateColor by animateColorAsState(targetValue = stateColor, animationSpec = tween(400), label = "stateColor")
    val haptic = LocalHapticFeedback.current

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("timer_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Mode Switcher (Stopwatch vs Pomodoro)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Surface(
                    onClick = { onTimerModeChange(TimerMode.STOPWATCH) },
                    enabled = !isSessionActive,
                    modifier = Modifier.weight(1f).height(36.dp),
                    shape = RoundedCornerShape(10.dp),
                    color = if (timerMode == TimerMode.STOPWATCH) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                    contentColor = if (timerMode == TimerMode.STOPWATCH) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Stopwatch", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                    }
                }

                Surface(
                    onClick = { onTimerModeChange(TimerMode.POMODORO) },
                    enabled = !isSessionActive,
                    modifier = Modifier.weight(1f).height(36.dp),
                    shape = RoundedCornerShape(10.dp),
                    color = if (timerMode == TimerMode.POMODORO) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                    contentColor = if (timerMode == TimerMode.POMODORO) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(imageVector = Icons.Default.Timer, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Pomodoro", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Top Status Header (State & Wanders)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // State Badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = animatedStateColor.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, animatedStateColor.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(animatedStateColor)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (timerMode == TimerMode.POMODORO) {
                                "${pomodoroPhase.displayName.uppercase()} (Cycle $pomodoroCurrentCycle/$pomodoroTargetCycles)"
                            } else {
                                currentState.displayName
                            },
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.1.sp
                            ),
                            color = animatedStateColor
                        )
                    }
                }

                // Passive Distraction (Mind Wander) Pill
                androidx.compose.animation.AnimatedVisibility(visible = currentState == FocusState.Working) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF59E0B).copy(alpha = 0.15f),
                        onClick = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); onLogDistraction() },
                        modifier = Modifier.testTag("distraction_badge")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Psychology,
                                contentDescription = "Mind wander",
                                tint = Color(0xFFF59E0B),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "+1 ($distractionCount)",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = Color(0xFFF59E0B)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Main Timer Display Area
            if (timerMode == TimerMode.POMODORO) {
                // Pomodoro Countdown & Progress
                val totalPhaseSeconds = when (pomodoroPhase) {
                    PomodoroPhase.WORK -> pomodoroWorkMinutes * 60L
                    PomodoroPhase.SHORT_BREAK -> pomodoroShortBreakMinutes * 60L
                    PomodoroPhase.LONG_BREAK -> 15 * 60L
                }
                val progress = if (totalPhaseSeconds > 0) {
                    (pomodoroRemainingSeconds.toFloat() / totalPhaseSeconds.toFloat()).coerceIn(0f, 1f)
                } else 1f
                val animProgress by animateFloatAsState(targetValue = progress, label = "pomodoro_progress")

                Text(
                    text = TimeFormatter.formatDuration(pomodoroRemainingSeconds),
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    ),
                    color = if (isSessionActive) animatedStateColor else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.testTag("timer_display")
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Progress Bar
                LinearProgressIndicator(
                    progress = { animProgress },
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = animatedStateColor,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    strokeCap = StrokeCap.Round
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Presets row if session is not active
                if (!isSessionActive) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        PomodoroPresetChip(
                            label = "25 / 5",
                            isSelected = pomodoroWorkMinutes == 25 && pomodoroShortBreakMinutes == 5,
                            onClick = { onPomodoroPresetSelect(25, 5) }
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        PomodoroPresetChip(
                            label = "45 / 10",
                            isSelected = pomodoroWorkMinutes == 45 && pomodoroShortBreakMinutes == 10,
                            onClick = { onPomodoroPresetSelect(45, 10) }
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        PomodoroPresetChip(
                            label = "50 / 10",
                            isSelected = pomodoroWorkMinutes == 50 && pomodoroShortBreakMinutes == 10,
                            onClick = { onPomodoroPresetSelect(50, 10) }
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        IconButton(
                            onClick = onCustomPomodoroClick,
                            modifier = Modifier.size(32.dp).background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = "Custom Pomodoro",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                // Classic Stopwatch Display
                Text(
                    text = TimeFormatter.formatDuration(blockElapsedSeconds),
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    ),
                    color = if (isSessionActive) animatedStateColor else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.testTag("timer_display")
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = if (isSessionActive) {
                    "Total Session: ${TimeFormatter.formatDuration(totalSessionElapsedSeconds)}"
                } else {
                    if (timerMode == TimerMode.POMODORO) "Pomodoro timer ready (Cycle $pomodoroCurrentCycle/$pomodoroTargetCycles)" else "Ready to start focus session"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Action Controls
            if (isSessionActive) {
                if (timerMode == TimerMode.POMODORO) {
                    // Pomodoro Active Controls
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilledTonalButton(
                            onClick = onSkipPomodoroPhase,
                            modifier = Modifier.weight(1f).height(46.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.SkipNext, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Skip Phase", style = MaterialTheme.typography.labelMedium)
                        }

                        OutlinedButton(
                            onClick = onResetPomodoro,
                            modifier = Modifier.weight(1f).height(46.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Reset Cycle", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                } else {
                    // Stopwatch State Selector Buttons (W, B, S)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StateButton(
                            label = "WORK",
                            icon = Icons.Default.PlayArrow,
                            isSelected = currentState == FocusState.Working,
                            activeColor = FocusState.Working.color(),
                            onClick = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); onStateChange(FocusState.Working) },
                            modifier = Modifier.weight(1f).testTag("btn_state_work")
                        )

                        StateButton(
                            label = "BREAK",
                            icon = Icons.Default.Coffee,
                            isSelected = currentState == FocusState.Break,
                            activeColor = FocusState.Break.color(),
                            onClick = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); onStateChange(FocusState.Break) },
                            modifier = Modifier.weight(1f).testTag("btn_state_break")
                        )

                        StateButton(
                            label = "SLACK",
                            icon = Icons.Default.Warning,
                            isSelected = currentState == FocusState.Procrastination,
                            activeColor = FocusState.Procrastination.color(),
                            onClick = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); onStateChange(FocusState.Procrastination) },
                            modifier = Modifier.weight(1f).testTag("btn_state_slack")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // End Session Button
                OutlinedButton(
                    onClick = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); onEndSession() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("btn_end_session"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
                ) {
                    Icon(imageVector = Icons.Default.Pause, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("End Session & Export Log", fontWeight = FontWeight.SemiBold)
                }
            } else {
                // Session Not Started -> Primary Start Action
                Button(
                    onClick = { onStartSession(if (timerMode == TimerMode.POMODORO) pomodoroPhase.focusState else FocusState.Working) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("btn_start_work"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = FocusState.Working.color()
                    )
                ) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (timerMode == TimerMode.POMODORO) "Start Pomodoro Work ($pomodoroWorkMinutes min)" else "Start Working",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun PomodoroPresetChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun StateButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    activeColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) activeColor else MaterialTheme.colorScheme.surfaceVariant,
        contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
        border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                )
            )
        }
    }
}
