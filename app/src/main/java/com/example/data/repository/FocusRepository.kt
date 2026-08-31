package com.example.data.repository

import com.example.data.dao.FocusDao
import com.example.data.entity.DeferredTaskEntity
import com.example.data.entity.FocusSessionEntity
import com.example.data.entity.GoalEntity
import com.example.data.entity.SessionWithDetails
import com.example.data.entity.TimelineBlockEntity
import kotlinx.coroutines.flow.Flow

class FocusRepository(private val focusDao: FocusDao) {

    val allSessions: Flow<List<FocusSessionEntity>> = focusDao.getAllSessions()
    val allSessionsWithDetails: Flow<List<SessionWithDetails>> = focusDao.getAllSessionsWithDetails()
    val activeGoals: Flow<List<GoalEntity>> = focusDao.getActiveAndPendingGoals()
    val allGoals: Flow<List<GoalEntity>> = focusDao.getAllGoals()
    val allDeferredTasks: Flow<List<DeferredTaskEntity>> = focusDao.getAllDeferredTasks()

    suspend fun getSessionWithDetails(sessionId: Long): SessionWithDetails? {
        return focusDao.getSessionWithDetailsById(sessionId)
    }

    suspend fun saveCompletedSession(
        session: FocusSessionEntity,
        timelineBlocks: List<TimelineBlockEntity>,
        completedGoals: List<GoalEntity>,
        sessionDeferredTasks: List<DeferredTaskEntity>
    ): Long {
        val sessionId = focusDao.insertSession(session)

        // Attach sessionId to timeline blocks and insert
        val blocksWithSession = timelineBlocks.mapIndexed { index, block ->
            block.copy(sessionId = sessionId, orderIndex = index)
        }
        if (blocksWithSession.isNotEmpty()) {
            focusDao.insertTimelineBlocks(blocksWithSession)
        }

        // Attach sessionId to completed goals in this session
        completedGoals.forEach { goal ->
            focusDao.updateGoal(goal.copy(sessionId = sessionId))
        }

        // Attach sessionId to deferred tasks created in this session
        sessionDeferredTasks.forEach { task ->
            focusDao.insertDeferredTask(task.copy(sessionId = sessionId))
        }

        return sessionId
    }

    suspend fun addGoal(description: String, groupName: String = "General"): Long {
        val goal = GoalEntity(
            description = description.trim(),
            isCompleted = false,
            isCarriedOver = false,
            groupName = groupName
        )
        return focusDao.insertGoal(goal)
    }

    suspend fun updateGoal(goal: GoalEntity) {
        focusDao.updateGoal(goal)
    }

    suspend fun deleteGoal(goalId: Long) {
        focusDao.deleteGoalById(goalId)
    }

    suspend fun clearCompletedActiveGoals() {
        focusDao.clearCompletedActiveGoals()
    }

    suspend fun addDeferredTask(text: String, groupName: String = "General", sessionId: Long? = null): Long {
        val task = DeferredTaskEntity(
            text = text.trim(),
            groupName = groupName,
            sessionId = sessionId,
            isCompleted = false
        )
        return focusDao.insertDeferredTask(task)
    }

    suspend fun updateDeferredTask(task: DeferredTaskEntity) {
        focusDao.updateDeferredTask(task)
    }

    suspend fun deleteDeferredTask(taskId: Long) {
        focusDao.deleteDeferredTaskById(taskId)
    }

    suspend fun deleteSession(sessionId: Long) {
        focusDao.deleteSessionById(sessionId)
    }

    suspend fun clearAllData() {
        focusDao.clearAllSessions()
        focusDao.clearCompletedActiveGoals()
    }
}
