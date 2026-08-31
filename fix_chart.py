import re

with open('app/src/main/java/com/example/ui/components/SevenDayBreakdownChart.kt', 'r') as f:
    content = f.read()

# Update signature
old_sig = """fun SevenDayBreakdownChart(
    dailySummaries: List<com.example.domain.model.DailySummary>,
    modifier: Modifier = Modifier
) {"""
new_sig = """fun SevenDayBreakdownChart(
    dailySummaries: List<com.example.domain.model.DailySummary>,
    period: com.example.ui.viewmodel.AnalyticsPeriod = com.example.ui.viewmodel.AnalyticsPeriod.WEEK,
    modifier: Modifier = Modifier
) {"""

content = content.replace(old_sig, new_sig)

# Update Text "7-Day Focus vs Distraction"
old_text = """                        Text(
                            text = "7-Day Focus vs Distraction",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )"""

new_text = """                        val titleText = if (period == com.example.ui.viewmodel.AnalyticsPeriod.WEEK) "7-Day Focus vs Distraction" else "4-Month Focus vs Distraction"
                        Text(
                            text = titleText,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )"""

content = content.replace(old_text, new_text)

# Update 7-Day KPI Strings
old_kpi = """            // 7-Day KPI Summary Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ChartKpi("7-Day Work", TimeFormatter.formatShortDuration(total7DayWork), workColor)"""

new_kpi = """            // KPI Summary Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val workLabel = if (period == com.example.ui.viewmodel.AnalyticsPeriod.WEEK) "7-Day Work" else "4-Mon Work"
                ChartKpi(workLabel, TimeFormatter.formatShortDuration(total7DayWork), workColor)"""

content = content.replace(old_kpi, new_kpi)

with open('app/src/main/java/com/example/ui/components/SevenDayBreakdownChart.kt', 'w') as f:
    f.write(content)


# Update AnalyticsScreen.kt call
with open('app/src/main/java/com/example/ui/screens/AnalyticsScreen.kt', 'r') as f:
    analytics_content = f.read()

analytics_content = analytics_content.replace(
    "SevenDayBreakdownChart(dailySummaries = analytics.dailySummaries)",
    "SevenDayBreakdownChart(dailySummaries = analytics.dailySummaries, period = uiState.analyticsPeriod)"
)

with open('app/src/main/java/com/example/ui/screens/AnalyticsScreen.kt', 'w') as f:
    f.write(analytics_content)

