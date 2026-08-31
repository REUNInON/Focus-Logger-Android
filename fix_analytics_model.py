with open('app/src/main/java/com/example/domain/model/DailyAnalytics.kt', 'r') as f:
    content = f.read()

content = content.replace(
"""    val sessionCount: Int,
    val completedGoalsCount: Int,
    val sessions: List<SessionWithDetails>
) {""",
"""    val sessionCount: Int,
    val completedGoalsCount: Int,
    val sessions: List<SessionWithDetails>,
    val timePerTask: Map<String, Long> = emptyMap()
) {""")

content = content.replace(
"""    val averageSessionSeconds: Long = 0,
    val overallEfficiency: Float = 0f,
    val currentStreak: Int = 0,
    val dailySummaries: List<DailySummary> = emptyList()
)""",
"""    val averageSessionSeconds: Long = 0,
    val overallEfficiency: Float = 0f,
    val currentStreak: Int = 0,
    val dailySummaries: List<DailySummary> = emptyList(),
    val overallTimePerTask: Map<String, Long> = emptyMap()
)""")

with open('app/src/main/java/com/example/domain/model/DailyAnalytics.kt', 'w') as f:
    f.write(content)
