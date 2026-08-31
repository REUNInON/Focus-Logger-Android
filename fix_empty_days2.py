import re

with open('app/src/main/java/com/example/ui/viewmodel/FocusViewModel.kt', 'r') as f:
    content = f.read()

# Instead of relying on exact string matching, we'll find `val groupedByDate = LinkedHashMap<String, MutableList<SessionWithDetails>>()`
# and insert the pre-fill logic right after it.

old_str = "val groupedByDate = LinkedHashMap<String, MutableList<SessionWithDetails>>()"

new_str = """val groupedByDate = LinkedHashMap<String, MutableList<SessionWithDetails>>()
        val keyFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
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

content = content.replace(old_str, new_str)

with open('app/src/main/java/com/example/ui/viewmodel/FocusViewModel.kt', 'w') as f:
    f.write(content)
