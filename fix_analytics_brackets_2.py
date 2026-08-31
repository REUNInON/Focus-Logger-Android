with open('app/src/main/java/com/example/ui/screens/AnalyticsScreen.kt', 'r') as f:
    content = f.read()

content = content.replace("    }\n}\n\n@Composable\nprivate fun MetricCard", "}\n\n@Composable\nprivate fun MetricCard")

with open('app/src/main/java/com/example/ui/screens/AnalyticsScreen.kt', 'w') as f:
    f.write(content)
