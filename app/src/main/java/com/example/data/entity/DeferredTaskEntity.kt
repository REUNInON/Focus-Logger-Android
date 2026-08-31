package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "deferred_tasks")
data class DeferredTaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val text: String,
    val createdAtTimestamp: Long = System.currentTimeMillis(),
    val sessionId: Long? = null,
    val isCompleted: Boolean = false,
    val groupName: String = "General"
)
