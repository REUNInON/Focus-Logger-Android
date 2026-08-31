import re

with open('app/src/main/java/com/example/ui/components/TimerCard.kt', 'r') as f:
    content = f.read()

imports = """import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType"""

if "LocalHapticFeedback" not in content:
    content = content.replace("import androidx.compose.foundation.layout.width", imports + "\nimport androidx.compose.foundation.layout.width")

haptic_init = """    onPomodoroSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current"""

content = content.replace("""    onPomodoroSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {""", haptic_init)

content = content.replace("onClick = onToggleTimer", "onClick = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); onToggleTimer() }")
content = content.replace("onClick = onStopTimer", "onClick = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); onStopTimer() }")
content = content.replace("onClick = onPomodoroSkip", "onClick = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); onPomodoroSkip() }")
content = content.replace("onClick = onPomodoroReset", "onClick = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); onPomodoroReset() }")

with open('app/src/main/java/com/example/ui/components/TimerCard.kt', 'w') as f:
    f.write(content)
