import re

with open('app/src/main/java/com/example/ui/screens/AnalyticsScreen.kt', 'r') as f:
    content = f.read()

imports = """import com.example.ui.viewmodel.AnalyticsPeriod
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.IconButton
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.foundation.layout.fillMaxWidth"""

if "import com.example.ui.viewmodel.AnalyticsPeriod" not in content:
    content = content.replace("import androidx.compose.foundation.layout.BoxWithConstraints", imports + "\nimport androidx.compose.foundation.layout.BoxWithConstraints")

# Insert Date Range Selector above the chart
old_header = """            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Performance & Analysis",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Day-by-day and session-by-session insights into your deep work and focus distribution.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp, bottom = 4.dp)
                )
            }"""

new_header = """            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Performance & Analysis",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Period Toggle
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    SegmentedButton(
                        selected = uiState.analyticsPeriod == AnalyticsPeriod.WEEK,
                        onClick = { viewModel.setAnalyticsPeriod(AnalyticsPeriod.WEEK) },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                    ) {
                        Text("Weekly")
                    }
                    SegmentedButton(
                        selected = uiState.analyticsPeriod == AnalyticsPeriod.MONTH,
                        onClick = { viewModel.setAnalyticsPeriod(AnalyticsPeriod.MONTH) },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                    ) {
                        Text("Monthly")
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Date Range Navigator
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { viewModel.shiftAnalyticsOffset(-1) }) {
                        Icon(imageVector = Icons.Default.ChevronLeft, contentDescription = "Previous")
                    }
                    
                    val cal = Calendar.getInstance()
                    val dateLabel = if (uiState.analyticsPeriod == AnalyticsPeriod.WEEK) {
                        cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
                        cal.add(Calendar.WEEK_OF_YEAR, uiState.analyticsOffset)
                        val startFormat = SimpleDateFormat("MMM d", Locale.getDefault()).format(cal.time)
                        cal.add(Calendar.DAY_OF_YEAR, 6)
                        val endFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(cal.time)
                        "$startFormat - $endFormat"
                    } else {
                        cal.set(Calendar.DAY_OF_MONTH, 1)
                        cal.add(Calendar.MONTH, uiState.analyticsOffset)
                        SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(cal.time)
                    }
                    
                    Text(
                        text = dateLabel,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    IconButton(
                        onClick = { viewModel.shiftAnalyticsOffset(1) },
                        enabled = uiState.analyticsOffset < 0
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Next",
                            tint = if (uiState.analyticsOffset < 0) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        )
                    }
                }
            }"""

content = content.replace(old_header, new_header)

# Ensure the chart takes the correct sessions. Wait, the chart was taking uiState.pastSessions (which is ALL sessions).
# We should filter it to uiState.overallAnalytics.sessions (we don't have that directly, but dailySummaries contains them).
# Or we can just use `uiState.pastSessions` and the chart itself handles the 7-day display.
# But now the chart should show the data for the selected period!
# Let's check `SevenDayBreakdownChart.kt`. We should pass `analyticsPeriod`, `analyticsOffset`, and `pastSessions` to it so it can calculate properly.
# Actually, `overallAnalytics.dailySummaries` already contains the summarized data for the selected period!
# We can just use `overallAnalytics.dailySummaries` for the chart.

with open('app/src/main/java/com/example/ui/screens/AnalyticsScreen.kt', 'w') as f:
    f.write(content)
