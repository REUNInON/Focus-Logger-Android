with open('app/src/main/java/com/example/ui/screens/AnalyticsScreen.kt', 'r') as f:
    content = f.read()

content = content.replace("import androidx.compose.foundation.layout.BoxWithConstraints", "import androidx.compose.foundation.layout.BoxWithConstraints\nimport androidx.compose.foundation.layout.fillMaxHeight")

with open('app/src/main/java/com/example/ui/screens/AnalyticsScreen.kt', 'w') as f:
    f.write(content)
