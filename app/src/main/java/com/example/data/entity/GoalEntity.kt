package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val description: String,
    val isCompleted: Boolean = false,
    val completedAtSessionSeconds: Long? = null,
    val createdAtTimestamp: Long = System.currentTimeMillis(),
    val completedAtTimestamp: Long? = null,
    val orderIndex: Int = 0,
    val sessionId: Long? = null,
    val isCarriedOver: Boolean = false,
    val groupName: String = "General",
    val totalWorkSecondsSpent: Long = 0L
)
