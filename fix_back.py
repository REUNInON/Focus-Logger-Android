import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

imports = """import androidx.activity.compose.BackHandler
import android.widget.Toast
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import kotlinx.coroutines.delay
import androidx.compose.runtime.LaunchedEffect"""

if "BackHandler" not in content:
    content = content.replace("import androidx.compose.material3.Surface", imports + "\nimport androidx.compose.material3.Surface")

# Wrap NavHost inside a DoubleBackHandler if we are on the FocusScreen
# But wait, it's easier to put the BackHandler directly in FocusScreen, and in SettingsScreen to popBackStack.
