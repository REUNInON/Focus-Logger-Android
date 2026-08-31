import re

with open('app/src/main/java/com/example/ui/screens/AnalyticsScreen.kt', 'r') as f:
    content = f.read()

content = content.replace(
'''import com.example.ui.components.DayAnalysisCard
import com.example.ui.components.SevenDayBreakdownChart''',
'''import com.example.ui.components.DayAnalysisCard
import com.example.ui.components.SevenDayBreakdownChart
import com.example.ui.components.WeeklyHeatmap''')

content = content.replace(
'''                SevenDayBreakdownChart(sessions = uiState.pastSessions)
                Spacer(modifier = Modifier.height(24.dp))''',
'''                SevenDayBreakdownChart(sessions = uiState.pastSessions)
                Spacer(modifier = Modifier.height(24.dp))
                WeeklyHeatmap(dailySummaries = analytics.dailySummaries)
                Spacer(modifier = Modifier.height(24.dp))''')

with open('app/src/main/java/com/example/ui/screens/AnalyticsScreen.kt', 'w') as f:
    f.write(content)
