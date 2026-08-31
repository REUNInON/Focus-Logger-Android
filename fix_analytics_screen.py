import re

with open('app/src/main/java/com/example/ui/screens/AnalyticsScreen.kt', 'r') as f:
    content = f.read()

# Add a section to show Time Spent per Task in AnalyticsListContent
# AnalyticsListContent has:
#         item {
#             Text(
#                 text = "Session by Session (${uiState.pastSessions.size})",
#                 style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
#                 modifier = Modifier.padding(bottom = 12.dp)
#             )
#         }

# We can add it after the Heatmap. Heatmap is called inside:
#                 WeeklyHeatmap(dailySummaries = analytics.dailySummaries)
#                 Spacer(modifier = Modifier.height(24.dp))

task_section = """                WeeklyHeatmap(dailySummaries = analytics.dailySummaries)
                Spacer(modifier = Modifier.height(24.dp))
                
                if (analytics.overallTimePerTask.isNotEmpty()) {
                    Text(
                        text = "Time Spent per Task",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    
                    val sortedTasks = analytics.overallTimePerTask.entries.sortedByDescending { it.value }
                    sortedTasks.forEach { (task, duration) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = task,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = com.example.domain.util.TimeFormatter.formatSeconds(duration),
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        androidx.compose.material3.Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }"""

content = content.replace(
'''                WeeklyHeatmap(dailySummaries = analytics.dailySummaries)
                Spacer(modifier = Modifier.height(24.dp))''', task_section)

with open('app/src/main/java/com/example/ui/screens/AnalyticsScreen.kt', 'w') as f:
    f.write(content)
