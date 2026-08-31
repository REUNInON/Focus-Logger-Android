import re

with open('app/src/main/java/com/example/ui/screens/FocusScreen.kt', 'r') as f:
    content = f.read()

# Fix clip.CircleShape), back to clip(CircleShape)
content = content.replace("clip.CircleShape),", "clip(CircleShape)")

# Fix Check icon unresolved
content = content.replace("Icons.Default.Check", "androidx.compose.material.icons.filled.Check")

if "import androidx.compose.material.icons.filled.Check" not in content:
    content = content.replace("import androidx.compose.material.icons.filled.Timer", "import androidx.compose.material.icons.filled.Check\nimport androidx.compose.material.icons.filled.Timer")

content = content.replace("androidx.compose.material.icons.filled.Check", "Icons.Default.Check")

# Fix line 218 comma
bad_box = """.background(MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.1f), androidx.compose.foundation.shape.CircleShape),
                                    contentAlignment = Alignment.Center"""
good_box = """.background(MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.1f), CircleShape),
                                    contentAlignment = Alignment.Center"""
content = content.replace(bad_box, good_box)

with open('app/src/main/java/com/example/ui/screens/FocusScreen.kt', 'w') as f:
    f.write(content)

