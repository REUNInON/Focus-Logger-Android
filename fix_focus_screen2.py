import re

with open('app/src/main/java/com/example/ui/screens/FocusScreen.kt', 'r') as f:
    content = f.read()

# Fix context injection
old_val = "val selectedGoalObj = uiState.activeGoals.find { it.id == uiState.selectedGoalId }"
new_val = """val selectedGoalObj = uiState.activeGoals.find { it.id == uiState.selectedGoalId }
    val context = LocalContext.current"""
content = content.replace(old_val, new_val)

# Fix isZenMode to isZenModeEnabled
content = content.replace("!uiState.isZenMode,", "!uiState.isZenModeEnabled,")

with open('app/src/main/java/com/example/ui/screens/FocusScreen.kt', 'w') as f:
    f.write(content)
