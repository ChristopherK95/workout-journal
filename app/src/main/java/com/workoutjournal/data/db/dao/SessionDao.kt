package com.workoutjournal.data.db.dao

import androidx.room.*
import com.workoutjournal.data.db.entity.SessionEntity
import kotlinx.coroutines.flow.Flow

data class SessionSummaryRaw(
    val id: Long,
    val dateEpochDay: Long,
    val name: String,
    val exerciseCount: Int,
    val startedAtEpochSecond: Long,
    val endedAtEpochSecond: Long,
    val notes: String
)

@Dao
interface SessionDao {
    @Query("""
        SELECT s.id, s.dateEpochDay, s.name, COUNT(e.id) AS exerciseCount,
               s.startedAtEpochSecond, s.endedAtEpochSecond, s.notes
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

    @Query("UPDATE sessions SET startedAtEpochSecond = :startedAt WHERE id = :id AND startedAtEpochSecond = 0")
    suspend fun setStartedAt(id: Long, startedAt: Long)

    @Query("UPDATE sessions SET endedAtEpochSecond = :endedAt WHERE id = :id")
    suspend fun setEndedAt(id: Long, endedAt: Long)

    @Query("UPDATE sessions SET name = :name WHERE id = :id")
    suspend fun updateName(id: Long, name: String)

    @Query("UPDATE sessions SET notes = :notes WHERE id = :id")
    suspend fun updateNotes(id: Long, notes: String)
}
