import re

with open('app/src/main/java/com/example/ui/screens/FocusScreen.kt', 'r') as f:
    content = f.read()

# Add imports
if 'import android.widget.Toast' not in content:
    content = content.replace('import androidx.compose.runtime.Composable', 'import android.widget.Toast\nimport androidx.compose.ui.platform.LocalContext\nimport androidx.compose.runtime.Composable')

# Add val context = LocalContext.current
if 'val context = LocalContext.current' not in content:
    content = content.replace('val uiState by viewModel.uiState.collectAsStateWithLifecycle()', 'val uiState by viewModel.uiState.collectAsStateWithLifecycle()\n    val context = LocalContext.current')

# Update Zen Mode in TimerNotificationPanel
old_panel_active = "isSessionActive = uiState.isSessionActive && uiState.showNotificationPanel,"
new_panel_active = "isSessionActive = uiState.isSessionActive && uiState.showNotificationPanel && !uiState.isZenMode,"
content = content.replace(old_panel_active, new_panel_active)

# Update onLogDistraction in TimerNotificationPanel
old_log_1 = "onLogDistraction = { viewModel.logDistraction() },"
new_log_1 = """onLogDistraction = { 
                        viewModel.logDistraction()
                        Toast.makeText(context, "Mind Wander Logged! (Total: ${uiState.distractionCount + 1})", Toast.LENGTH_SHORT).show()
                    },"""
content = content.replace(old_log_1, new_log_1, 1)

# Update onLogDistraction in TimerCard
old_log_2 = "onLogDistraction = { viewModel.logDistraction() }"
new_log_2 = """onLogDistraction = { 
                        viewModel.logDistraction()
                        Toast.makeText(context, "Mind Wander Logged! (Total: ${uiState.distractionCount + 1})", Toast.LENGTH_SHORT).show()
                    }"""
content = content.replace(old_log_2, new_log_2, 1)

with open('app/src/main/java/com/example/ui/screens/FocusScreen.kt', 'w') as f:
    f.write(content)
