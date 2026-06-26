package com.workoutjournal.data.db.dao

import androidx.room.*
import com.workoutjournal.data.db.entity.ExerciseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseDao {
    @Query("SELECT * FROM exercises WHERE sessionId = :sessionId ORDER BY orderIndex ASC")
    fun getExercisesBySession(sessionId: Long): Flow<List<ExerciseEntity>>

    @Query("SELECT DISTINCT name FROM exercises ORDER BY name ASC")
    fun getAllExerciseNames(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercise(exercise: ExerciseEntity): Long

    @Update
    suspend fun updateExercise(exercise: ExerciseEntity)

    @Query("DELETE FROM exercises WHERE id = :id")
    suspend fun deleteExerciseById(id: Long)
}
