package com.workoutjournal.data.db.dao

import androidx.room.*
import com.workoutjournal.data.db.entity.SessionEntity
import kotlinx.coroutines.flow.Flow

data class SessionSummaryRaw(
    val id: Long,
    val dateEpochDay: Long,
    val name: String,
    val exerciseCount: Int
)

@Dao
interface SessionDao {
    @Query("""
        SELECT s.id, s.dateEpochDay, s.name, COUNT(e.id) AS exerciseCount
        FROM sessions s
        LEFT JOIN exercises e ON e.sessionId = s.id
        GROUP BY s.id
        ORDER BY s.dateEpochDay DESC
    """)
    fun getAllSessionSummaries(): Flow<List<SessionSummaryRaw>>

    @Query("SELECT * FROM sessions WHERE id = :id")
    fun getSessionById(id: Long): Flow<SessionEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: SessionEntity): Long

    @Update
    suspend fun updateSession(session: SessionEntity)

    @Delete
    suspend fun deleteSession(session: SessionEntity)
}
