import re

with open('app/src/main/java/com/example/ui/viewmodel/FocusViewModel.kt', 'r') as f:
    content = f.read()

# I need to fix how displayDate is formatted inside the groupedByDate.map block
old_daily = """            val firstTimestamp = list.firstOrNull()?.session?.startTime ?: System.currentTimeMillis()
            DailySummary(
                dateString = dateKey,
                displayDate = TimeFormatter.formatDisplayDate(firstTimestamp),"""

new_daily = """            val parsedDate = keyFormat.parse(dateKey) ?: java.util.Date()
            DailySummary(
                dateString = dateKey,
                displayDate = java.text.SimpleDateFormat("MMM d", java.util.Locale.getDefault()).format(parsedDate),"""

content = content.replace(old_daily, new_daily)

with open('app/src/main/java/com/example/ui/viewmodel/FocusViewModel.kt', 'w') as f:
    f.write(content)
