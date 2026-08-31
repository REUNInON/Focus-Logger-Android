package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "focus_sessions")
data class FocusSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val startTime: Long,
    val endTime: Long,
    val totalWorkSeconds: Long,
    val totalBreakSeconds: Long,
    val totalSlackSeconds: Long,
    val distractionCount: Int,
    val completedGoalCount: Int,
    val pendingGoalCount: Int
) {
    val totalDurationSeconds: Long
        get() = totalWorkSeconds + totalBreakSeconds + totalSlackSeconds

    val focusEfficiencyRatio: Float
        get() {
            val total = totalDurationSeconds
            return if (total > 0) (totalWorkSeconds.toFloat() / total.toFloat()) * 100f else 0f
        }
}
