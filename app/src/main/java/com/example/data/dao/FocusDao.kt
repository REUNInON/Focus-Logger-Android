package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.entity.DeferredTaskEntity
import com.example.data.entity.FocusSessionEntity
import com.example.data.entity.GoalEntity
import com.example.data.entity.SessionWithDetails
import com.example.data.entity.TimelineBlockEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FocusDao {
    // Sessions
    @Query("SELECT * FROM focus_sessions ORDER BY startTime DESC")
    fun getAllSessions(): Flow<List<FocusSessionEntity>>

    @Transaction
    @Query("SELECT * FROM focus_sessions ORDER BY startTime DESC")
    fun getAllSessionsWithDetails(): Flow<List<SessionWithDetails>>

    @Transaction
    @Query("SELECT * FROM focus_sessions WHERE id = :sessionId LIMIT 1")
    suspend fun getSessionWithDetailsById(sessionId: Long): SessionWithDetails?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: FocusSessionEntity): Long

    @Query("DELETE FROM focus_sessions WHERE id = :sessionId")
    suspend fun deleteSessionById(sessionId: Long)

    @Query("DELETE FROM focus_sessions")
    suspend fun clearAllSessions()

    // Timeline Blocks
    @Query("SELECT * FROM timeline_blocks WHERE sessionId = :sessionId ORDER BY orderIndex ASC")
    fun getTimelineBlocksForSession(sessionId: Long): Flow<List<TimelineBlockEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTimelineBlocks(blocks: List<TimelineBlockEntity>)

    // Goals
    // Active goals (either not yet completed or completed recently in current active pool)
    @Query("SELECT * FROM goals WHERE isCompleted = 0 OR sessionId IS NULL ORDER BY isCompleted ASC, orderIndex ASC, id ASC")
    fun getActiveAndPendingGoals(): Flow<List<GoalEntity>>

    @Query("SELECT * FROM goals WHERE sessionId = :sessionId ORDER BY orderIndex ASC")
    fun getGoalsForSession(sessionId: Long): Flow<List<GoalEntity>>

    @Query("SELECT * FROM goals ORDER BY isCompleted ASC, orderIndex ASC, id DESC")
    fun getAllGoals(): Flow<List<GoalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: GoalEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoals(goals: List<GoalEntity>): List<Long>

    @Update
    suspend fun updateGoal(goal: GoalEntity)

    @Query("DELETE FROM goals WHERE id = :goalId")
    suspend fun deleteGoalById(goalId: Long)

    @Query("DELETE FROM goals WHERE isCompleted = 1 AND sessionId IS NULL")
    suspend fun clearCompletedActiveGoals()

    // Deferred Tasks (Mind Dump / Do-Later)
    @Query("SELECT * FROM deferred_tasks ORDER BY isCompleted ASC, createdAtTimestamp DESC")
    fun getAllDeferredTasks(): Flow<List<DeferredTaskEntity>>

    @Query("SELECT * FROM deferred_tasks WHERE sessionId = :sessionId OR sessionId IS NULL ORDER BY isCompleted ASC, createdAtTimestamp DESC")
    fun getDeferredTasksForActiveOrSession(sessionId: Long): Flow<List<DeferredTaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeferredTask(task: DeferredTaskEntity): Long

    @Update
    suspend fun updateDeferredTask(task: DeferredTaskEntity)

    @Query("DELETE FROM deferred_tasks WHERE id = :taskId")
    suspend fun deleteDeferredTaskById(taskId: Long)

    @Query("DELETE FROM deferred_tasks WHERE isCompleted = 1")
    suspend fun clearCompletedDeferredTasks()
}
