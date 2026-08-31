package com.example.data.entity

import androidx.room.Embedded
import androidx.room.Relation

data class SessionWithDetails(
    @Embedded val session: FocusSessionEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "sessionId"
    )
    val timelineBlocks: List<TimelineBlockEntity>,
    @Relation(
        parentColumn = "id",
        entityColumn = "sessionId"
    )
    val deferredTasks: List<DeferredTaskEntity>,
    @Relation(
        parentColumn = "id",
        entityColumn = "sessionId"
    )
    val goals: List<GoalEntity>
)
