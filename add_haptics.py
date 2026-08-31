import re

# TimerCard.kt
with open('app/src/main/java/com/example/ui/components/TimerCard.kt', 'r') as f:
    tc_content = f.read()

tc_old_wander = "onClick = onLogDistraction,"
tc_new_wander = "onClick = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); onLogDistraction() },"
tc_content = tc_content.replace(tc_old_wander, tc_new_wander)

with open('app/src/main/java/com/example/ui/components/TimerCard.kt', 'w') as f:
    f.write(tc_content)


# TimerNotificationPanel.kt
with open('app/src/main/java/com/example/ui/components/TimerNotificationPanel.kt', 'r') as f:
    tnp_content = f.read()

if 'import androidx.compose.ui.platform.LocalHapticFeedback' not in tnp_content:
    tnp_content = tnp_content.replace(
        'import androidx.compose.runtime.Composable',
        'import androidx.compose.ui.platform.LocalHapticFeedback\nimport androidx.compose.ui.hapticfeedback.HapticFeedbackType\nimport androidx.compose.runtime.Composable'
    )

tnp_old_sig = "modifier: Modifier = Modifier\n) {"
tnp_new_sig = "modifier: Modifier = Modifier\n) {\n    val haptic = LocalHapticFeedback.current"
tnp_content = tnp_content.replace(tnp_old_sig, tnp_new_sig)

tnp_content = tnp_content.replace(
    'onClick = { onStateChange(FocusState.Working) },',
    'onClick = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); onStateChange(FocusState.Working) },'
)
tnp_content = tnp_content.replace(
    'onClick = { onStateChange(FocusState.Break) },',
    'onClick = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); onStateChange(FocusState.Break) },'
)
tnp_content = tnp_content.replace(
    'onClick = { onStateChange(FocusState.Procrastination) },',
    'onClick = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); onStateChange(FocusState.Procrastination) },'
)
tnp_content = tnp_content.replace(
    'onClick = onLogDistraction,',
    'onClick = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); onLogDistraction() },'
)

with open('app/src/main/java/com/example/ui/components/TimerNotificationPanel.kt', 'w') as f:
    f.write(tnp_content)

