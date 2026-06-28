package com.workoutjournal.data.db.dao

import androidx.room.*
import com.workoutjournal.data.db.entity.SetEntity
import kotlinx.coroutines.flow.Flow

data class ProgressDataPoint(
    val dateEpochDay: Long,
    val maxWeightKg: Float
)

data class VolumeDataPoint(
    val dateEpochDay: Long,
    val totalVolume: Float
)

data class LastSetData(
    val weightKg: Float,
    val reps: Int
)

@Dao
interface SetDao {
    @Query("SELECT * FROM sets WHERE exerciseId = :exerciseId ORDER BY setNumber ASC")
    fun getSetsByExercise(exerciseId: Long): Flow<List<SetEntity>>

    @Query("""
        SELECT s.dateEpochDay, MAX(st.weightKg) AS maxWeightKg
        FROM sessions s
        JOIN exercises e ON e.sessionId = s.id
        JOIN sets st ON st.exerciseId = e.id
        WHERE e.name = :exerciseName
        GROUP BY s.id
        ORDER BY s.dateEpochDay ASC
    """)
    fun getProgressForExercise(exerciseName: String): Flow<List<ProgressDataPoint>>

    @Query("""
        SELECT st.weightKg, st.reps
        FROM sets st
        JOIN exercises e ON st.exerciseId = e.id
        JOIN sessions s ON e.sessionId = s.id
        WHERE e.name = :exerciseName AND s.id != :currentSessionId
        ORDER BY s.dateEpochDay DESC, st.weightKg DESC
        LIMIT 1
    """)
    suspend fun getLastBestSetForExercise(exerciseName: String, currentSessionId: Long): LastSetData?

    @Query("""
        SELECT st.weightKg, st.reps
        FROM sets st
        JOIN exercises e ON st.exerciseId = e.id
        WHERE e.name = :exerciseName
        ORDER BY st.weightKg DESC
        LIMIT 1
    """)
    suspend fun getAllTimeBestForExercise(exerciseName: String): LastSetData?

    @Query("""
        SELECT s.dateEpochDay, SUM(st.weightKg * st.reps) AS totalVolume
        FROM sessions s
        JOIN exercises e ON e.sessionId = s.id
        JOIN sets st ON st.exerciseId = e.id
        WHERE e.name = :exerciseName
        GROUP BY s.id
        ORDER BY s.dateEpochDay ASC
    """)
    fun getVolumePerSessionForExercise(exerciseName: String): Flow<List<VolumeDataPoint>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSet(set: SetEntity): Long

    @Query("UPDATE sets SET weightKg = :weightKg, reps = :reps WHERE id = :id")
    suspend fun updateSetValues(id: Long, weightKg: Float, reps: Int)

    @Query("DELETE FROM sets WHERE id = :id")
    suspend fun deleteSetById(id: Long)
}
