import re

with open('app/src/main/java/com/example/ui/viewmodel/FocusViewModel.kt', 'r') as f:
    content = f.read()

# 1. Update filterSessionsByPeriod
old_filter = """            AnalyticsPeriod.MONTH -> {
                cal.set(java.util.Calendar.DAY_OF_MONTH, 1)
                cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
                cal.set(java.util.Calendar.MINUTE, 0)
                cal.set(java.util.Calendar.SECOND, 0)
                cal.set(java.util.Calendar.MILLISECOND, 0)
                cal.add(java.util.Calendar.MONTH, offset)
                val start = cal.timeInMillis
                cal.add(java.util.Calendar.MONTH, 1)
                val end = cal.timeInMillis
                return sessions.filter { it.session.startTime in start until end }
            }"""

new_filter = """            AnalyticsPeriod.MONTH -> {
                cal.set(java.util.Calendar.DAY_OF_MONTH, 1)
                cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
                cal.set(java.util.Calendar.MINUTE, 0)
                cal.set(java.util.Calendar.SECOND, 0)
                cal.set(java.util.Calendar.MILLISECOND, 0)
                cal.add(java.util.Calendar.MONTH, offset)
                cal.add(java.util.Calendar.MONTH, 1)
                val end = cal.timeInMillis
                cal.add(java.util.Calendar.MONTH, -4)
                val start = cal.timeInMillis
                return sessions.filter { it.session.startTime in start until end }
            }"""

content = content.replace(old_filter, new_filter)

# 2. Update calculateAnalytics group prefilling
old_group = """        val keyFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        val calEmpty = java.util.Calendar.getInstance()
        if (period == com.example.ui.viewmodel.AnalyticsPeriod.WEEK) {
            calEmpty.set(java.util.Calendar.DAY_OF_WEEK, calEmpty.firstDayOfWeek)
            calEmpty.add(java.util.Calendar.WEEK_OF_YEAR, offset)
            for (i in 0..6) {
                groupedByDate[keyFormat.format(calEmpty.time)] = mutableListOf()
                calEmpty.add(java.util.Calendar.DAY_OF_YEAR, 1)
            }
        } else {
            calEmpty.set(java.util.Calendar.DAY_OF_MONTH, 1)
            calEmpty.add(java.util.Calendar.MONTH, offset)
            val maxDays = calEmpty.getActualMaximum(java.util.Calendar.DAY_OF_MONTH)
            for (i in 1..maxDays) {
                groupedByDate[keyFormat.format(calEmpty.time)] = mutableListOf()
                calEmpty.add(java.util.Calendar.DAY_OF_YEAR, 1)
            }
        }"""

new_group = """        val keyFormat = if (period == com.example.ui.viewmodel.AnalyticsPeriod.WEEK) {
            java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        } else {
            java.text.SimpleDateFormat("yyyy-MM", java.util.Locale.getDefault())
        }
        
        val calEmpty = java.util.Calendar.getInstance()
        if (period == com.example.ui.viewmodel.AnalyticsPeriod.WEEK) {
            calEmpty.set(java.util.Calendar.DAY_OF_WEEK, calEmpty.firstDayOfWeek)
            calEmpty.add(java.util.Calendar.WEEK_OF_YEAR, offset)
            for (i in 0..6) {
                groupedByDate[keyFormat.format(calEmpty.time)] = mutableListOf()
                calEmpty.add(java.util.Calendar.DAY_OF_YEAR, 1)
            }
        } else {
            calEmpty.set(java.util.Calendar.DAY_OF_MONTH, 1)
            calEmpty.add(java.util.Calendar.MONTH, offset - 3)
            for (i in 0..3) {
                groupedByDate[keyFormat.format(calEmpty.time)] = mutableListOf()
                calEmpty.add(java.util.Calendar.MONTH, 1)
            }
        }"""

content = content.replace(old_group, new_group)

# 3. Update grouping assignment to use the dynamic keyFormat
# We need to find: val dateKey = TimeFormatter.formatDateOnly(s.startTime)
# Wait, TimeFormatter.formatDateOnly uses "yyyy-MM-dd". We must use `keyFormat.format(...)`

old_assign = "val dateKey = TimeFormatter.formatDateOnly(s.startTime)"
new_assign = "val dateKey = keyFormat.format(java.util.Date(s.startTime))"

content = content.replace(old_assign, new_assign)

# 4. Update the display formatting inside the map
old_display = """            val parsedDate = keyFormat.parse(dateKey) ?: java.util.Date()
            DailySummary(
                dateString = dateKey,
                displayDate = java.text.SimpleDateFormat("MMM d", java.util.Locale.getDefault()).format(parsedDate),"""

new_display = """            val parsedDate = keyFormat.parse(dateKey) ?: java.util.Date()
            val displayFmt = if (period == com.example.ui.viewmodel.AnalyticsPeriod.WEEK) {
                java.text.SimpleDateFormat("MMM d", java.util.Locale.getDefault())
            } else {
                java.text.SimpleDateFormat("MMM", java.util.Locale.getDefault())
            }
            DailySummary(
                dateString = dateKey,
                displayDate = displayFmt.format(parsedDate),"""

content = content.replace(old_display, new_display)

with open('app/src/main/java/com/example/ui/viewmodel/FocusViewModel.kt', 'w') as f:
    f.write(content)
