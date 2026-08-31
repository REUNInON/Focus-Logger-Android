package com.example.domain.model

import com.example.data.entity.SessionWithDetails

data class DailySummary(
    val dateString: String,
    val displayDate: String,
    val totalWorkSeconds: Long,
    val totalBreakSeconds: Long,
    val totalSlackSeconds: Long,
    val totalDistractions: Int,
    val sessionCount: Int,
    val completedGoalsCount: Int,
    val sessions: List<SessionWithDetails>,
    val timePerTask: Map<String, Long> = emptyMap()
) {
    val totalDurationSeconds: Long
        get() = totalWorkSeconds + totalBreakSeconds + totalSlackSeconds

    val focusEfficiencyRatio: Float
        get() {
            val total = totalDurationSeconds
            return if (total > 0) (totalWorkSeconds.toFloat() / total.toFloat()) * 100f else 0f
        }
}

data class OverallAnalytics(
    val totalWorkSeconds: Long = 0,
    val totalBreakSeconds: Long = 0,
    val totalSlackSeconds: Long = 0,
    val totalDistractions: Int = 0,
    val totalSessions: Int = 0,
    val totalGoalsCompleted: Int = 0,
    val averageSessionSeconds: Long = 0,
    val overallEfficiency: Float = 0f,
    val currentStreak: Int = 0,
    val dailySummaries: List<DailySummary> = emptyList(),
    val overallTimePerTask: Map<String, Long> = emptyMap()
)
