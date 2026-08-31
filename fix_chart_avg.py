import re

with open('app/src/main/java/com/example/ui/components/SevenDayBreakdownChart.kt', 'r') as f:
    content = f.read()

old_avg = "val avgDailyWorkSec = total7DayWork / 7"
new_avg = "val avgDailyWorkSec = if (chartData.isNotEmpty()) total7DayWork / chartData.size else 0"

content = content.replace(old_avg, new_avg)

old_kpi2 = """ChartKpi("Daily Avg", TimeFormatter.formatShortDuration(avgDailyWorkSec), MaterialTheme.colorScheme.primary)"""
new_kpi2 = """val avgLabel = if (period == com.example.ui.viewmodel.AnalyticsPeriod.WEEK) "Daily Avg" else "Month Avg"
                ChartKpi(avgLabel, TimeFormatter.formatShortDuration(avgDailyWorkSec), MaterialTheme.colorScheme.primary)"""

content = content.replace(old_kpi2, new_kpi2)

with open('app/src/main/java/com/example/ui/components/SevenDayBreakdownChart.kt', 'w') as f:
    f.write(content)
