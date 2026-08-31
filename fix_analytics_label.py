import re

with open('app/src/main/java/com/example/ui/screens/AnalyticsScreen.kt', 'r') as f:
    content = f.read()

old_label = """                    } else {
                        cal.set(Calendar.DAY_OF_MONTH, 1)
                        cal.add(Calendar.MONTH, uiState.analyticsOffset)
                        SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(cal.time)
                    }"""

new_label = """                    } else {
                        cal.set(Calendar.DAY_OF_MONTH, 1)
                        cal.add(Calendar.MONTH, uiState.analyticsOffset)
                        val endM = SimpleDateFormat("MMM yyyy", Locale.getDefault()).format(cal.time)
                        cal.add(Calendar.MONTH, -3)
                        val startM = SimpleDateFormat("MMM", Locale.getDefault()).format(cal.time)
                        "$startM - $endM"
                    }"""

content = content.replace(old_label, new_label)

with open('app/src/main/java/com/example/ui/screens/AnalyticsScreen.kt', 'w') as f:
    f.write(content)
