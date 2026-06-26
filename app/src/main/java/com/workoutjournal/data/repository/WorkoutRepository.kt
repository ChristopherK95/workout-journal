package com.workoutjournal.data.repository

import com.workoutjournal.data.db.dao.ExerciseDao
import com.workoutjournal.data.db.dao.SessionDao
import com.workoutjournal.data.db.dao.SetDao
import com.workoutjournal.data.db.entity.ExerciseEntity
import com.workoutjournal.data.db.entity.SessionEntity
import com.workoutjournal.data.db.entity.SetEntity
import com.workoutjournal.domain.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import java.time.LocalDate

class WorkoutRepository(
    private val sessionDao: SessionDao,
    private val exerciseDao: ExerciseDao,
    private val setDao: SetDao
) {
    fun getAllSessionSummaries(): Flow<List<SessionSummary>> =
        sessionDao.getAllSessionSummaries().map { list ->
            list.map { raw ->
                SessionSummary(
                    id = raw.id,
                    date = LocalDate.ofEpochDay(raw.dateEpochDay),
                    name = raw.name,
                    exerciseCount = raw.exerciseCount
                )
            }
        }

    fun getSessionFlow(sessionId: Long): Flow<WorkoutSession?> =
        sessionDao.getSessionById(sessionId).map { it?.toModel() }

    suspend fun createSession(date: LocalDate, name: String = ""): Long =
        sessionDao.insertSession(SessionEntity(dateEpochDay = date.toEpochDay(), name = name))

    suspend fun updateSession(session: WorkoutSession) =
        sessionDao.updateSession(session.toEntity())

    suspend fun deleteSession(session: WorkoutSession) =
        sessionDao.deleteSession(session.toEntity())

    fun getExercisesWithSets(sessionId: Long): Flow<List<ExerciseWithSets>> =
        exerciseDao.getExercisesBySession(sessionId).flatMapLatest { exercises ->
            if (exercises.isEmpty()) {
                flowOf(emptyList())
            } else {
                combine(exercises.map { exercise ->
                    setDao.getSetsByExercise(exercise.id).map { sets ->
                        ExerciseWithSets(
                            exercise = exercise.toModel(),
                            sets = sets.map { it.toModel() }
                        )
                    }
                }) { it.toList() }
            }
        }

    fun getAllExerciseNames(): Flow<List<String>> =
        exerciseDao.getAllExerciseNames()

    suspend fun addExercise(sessionId: Long, name: String, orderIndex: Int): Long =
        exerciseDao.insertExercise(ExerciseEntity(sessionId = sessionId, name = name, orderIndex = orderIndex))

    suspend fun deleteExercise(exerciseId: Long) =
        exerciseDao.deleteExerciseById(exerciseId)

    suspend fun addSet(exerciseId: Long, setNumber: Int, weightKg: Float, reps: Int): Long =
        setDao.insertSet(SetEntity(exerciseId = exerciseId, setNumber = setNumber, weightKg = weightKg, reps = reps))

    suspend fun updateSet(setId: Long, weightKg: Float, reps: Int) =
        setDao.updateSetValues(setId, weightKg, reps)

    suspend fun deleteSet(setId: Long) =
        setDao.deleteSetById(setId)

    fun getProgressForExercise(exerciseName: String): Flow<List<ProgressPoint>> =
        setDao.getProgressForExercise(exerciseName).map { list ->
            list.map { ProgressPoint(date = LocalDate.ofEpochDay(it.dateEpochDay), maxWeightKg = it.maxWeightKg) }
        }

    suspend fun getLastBestSetForExercise(exerciseName: String, currentSessionId: Long): Pair<Float, Int>? =
        setDao.getLastBestSetForExercise(exerciseName, currentSessionId)?.let { it.weightKg to it.reps }
}

private fun SessionEntity.toModel() = WorkoutSession(id = id, date = LocalDate.ofEpochDay(dateEpochDay), name = name)
private fun WorkoutSession.toEntity() = SessionEntity(id = id, dateEpochDay = date.toEpochDay(), name = name)
private fun ExerciseEntity.toModel() = ExerciseEntry(id = id, sessionId = sessionId, name = name, orderIndex = orderIndex)
private fun SetEntity.toModel() = SetEntry(id = id, exerciseId = exerciseId, setNumber = setNumber, weightKg = weightKg, reps = reps)
