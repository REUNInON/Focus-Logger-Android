import re

with open('app/src/main/java/com/example/ui/screens/MainScreen.kt', 'r') as f:
    content = f.read()

imports = """import androidx.activity.compose.BackHandler
import android.widget.Toast
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay
import android.app.Activity
import androidx.compose.ui.platform.LocalContext"""

if "BackHandler" not in content:
    content = content.replace("import androidx.compose.material3.Text", imports + "\nimport androidx.compose.material3.Text")

back_handler = """    var selectedTabIndex by remember { mutableIntStateOf(0) }
    
    val context = LocalContext.current
    var backPressedOnce by remember { androidx.compose.runtime.mutableStateOf(false) }

    LaunchedEffect(backPressedOnce) {
        if (backPressedOnce) {
            delay(2000)
            backPressedOnce = false
        }
    }

    BackHandler {
        if (selectedTabIndex != 0) {
            selectedTabIndex = 0
        } else {
            if (backPressedOnce) {
                (context as? Activity)?.moveTaskToBack(true)
            } else {
                backPressedOnce = true
                Toast.makeText(context, "Press back again to exit (Timer will keep running)", Toast.LENGTH_SHORT).show()
            }
        }
    }"""

content = content.replace("    var selectedTabIndex by remember { mutableIntStateOf(0) }", back_handler)

with open('app/src/main/java/com/example/ui/screens/MainScreen.kt', 'w') as f:
    f.write(content)
