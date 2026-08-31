with open('app/src/main/java/com/example/ui/screens/HistoryScreen.kt', 'r') as f:
    content = f.read()

content = content.replace("        }\n    }\n\n\n@Composable\nprivate fun HistorySessionCard", "        }\n    }\n}\n\n@Composable\nprivate fun HistorySessionCard")

with open('app/src/main/java/com/example/ui/screens/HistoryScreen.kt', 'w') as f:
    f.write(content)
