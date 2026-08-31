import re

with open('app/src/main/java/com/example/ui/components/SessionSummaryDialog.kt', 'r') as f:
    content = f.read()

content = content.replace("            }\n        }\n    }\n}\n\n@Composable\nprivate fun StatPill", "            }\n        }\n    }\n\n@Composable\nprivate fun StatPill")

with open('app/src/main/java/com/example/ui/components/SessionSummaryDialog.kt', 'w') as f:
    f.write(content)

