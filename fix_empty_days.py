import re

with open('app/src/main/java/com/example/ui/viewmodel/FocusViewModel.kt', 'r') as f:
    content = f.read()

# We need to pass period and offset to calculateAnalytics to generate empty days, OR we can just do it in the UI/Chart.
# Doing it in the viewmodel is cleaner.
old_calc = """    private fun calculateAnalytics(sessions: List<SessionWithDetails>): OverallAnalytics {"""
new_calc = """    private fun calculateAnalytics(
        sessions: List<SessionWithDetails>,
        period: AnalyticsPeriod = AnalyticsPeriod.WEEK,
        offset: Int = 0
    ): OverallAnalytics {"""

content = content.replace(old_calc, new_calc)

# Update calls to calculateAnalytics
content = content.replace(
    "val analytics = calculateAnalytics(filteredSessions)",
    "val analytics = calculateAnalytics(filteredSessions, period, offset)"
)

content = content.replace(
    "val globalAnalytics = calculateAnalytics(sessions)",
    "val globalAnalytics = calculateAnalytics(sessions, AnalyticsPeriod.WEEK, 0)"
)

# Now, populate the map with empty days
old_group = """        val groupedByDate = LinkedHashMap<String, MutableList<SessionWithDetails>>()

        for (item in sessions) {"""

new_group = """        val groupedByDate = LinkedHashMap<String, MutableList<SessionWithDetails>>()
        
        // Pre-fill days
        val cal = java.util.Calendar.getInstance()
        val keyFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        
        if (period == AnalyticsPeriod.WEEK) {
            cal.set(java.util.Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
            cal.add(java.util.Calendar.WEEK_OF_YEAR, offset)
            for (i in 0..6) {
                groupedByDate[keyFormat.format(cal.time)] = mutableListOf()
                cal.add(java.util.Calendar.DAY_OF_YEAR, 1)
            }
        } else {
            cal.set(java.util.Calendar.DAY_OF_MONTH, 1)
            cal.add(java.util.Calendar.MONTH, offset)
            val maxDays = cal.getActualMaximum(java.util.Calendar.DAY_OF_MONTH)
            for (i in 1..maxDays) {
                groupedByDate[keyFormat.format(cal.time)] = mutableListOf()
                cal.add(java.util.Calendar.DAY_OF_YEAR, 1)
            }
        }

        for (item in sessions) {"""

content = content.replace(old_group, new_group)

# Also fix the displayDate logic since an empty list will have no firstTimestamp
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

