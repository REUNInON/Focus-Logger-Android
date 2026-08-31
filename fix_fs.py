import re

with open('app/src/main/java/com/example/ui/screens/FocusScreen.kt', 'r') as f:
    content = f.read()

# Fix the unresolved Check
content = content.replace("androidx.compose.material.icons.Icons.Filled.Check", "androidx.compose.material.icons.Icons.Default.Check")

# Wait, `Check` needs to be imported or use full path correctly.
# Icons.Default.Check IS correct in Jetpack Compose, it maps to Icons.Filled.Check but is accessed via Default.
# Let's ensure the import `import androidx.compose.material.icons.filled.Check` is present and just use `Icons.Default.Check`.

if "import androidx.compose.material.icons.filled.Check" not in content:
    content = content.replace("import androidx.compose.material.icons.filled.Timer", "import androidx.compose.material.icons.filled.Check\nimport androidx.compose.material.icons.filled.Timer")

content = content.replace("androidx.compose.material.icons.Icons.Filled.Check", "Icons.Default.Check")

# Fix the Modifier error
# Previous string was:
# .Modifier
# .androidx.compose.foundation.background(MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.1f)),
# We want:
# .background(MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.1f), androidx.compose.foundation.shape.CircleShape),
bad_modifier = """.Modifier
                                        .androidx.compose.foundation.background(MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.1f)),"""
good_modifier = """.background(MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.1f), androidx.compose.foundation.shape.CircleShape)"""

if bad_modifier in content:
    content = content.replace(bad_modifier, good_modifier)

with open('app/src/main/java/com/example/ui/screens/FocusScreen.kt', 'w') as f:
    f.write(content)
