package com.workoutjournal.data.repository

import com.workoutjournal.data.db.dao.ExerciseDao
import com.workoutjournal.data.db.dao.SessionDao
import com.workoutjournal.data.db.dao.SetDao
import com.workoutjournal.data.db.dao.TemplateDao
import com.workoutjournal.data.db.entity.ExerciseEntity
import com.workoutjournal.data.db.entity.SessionEntity
import com.workoutjournal.data.db.entity.SetEntity
import com.workoutjournal.data.db.entity.TemplateEntity
import com.workoutjournal.data.db.entity.TemplateExerciseEntity
import com.workoutjournal.data.db.dao.VolumeDataPoint
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
    private val setDao: SetDao,
    private val templateDao: TemplateDao
) {
    fun getAllSessionSummaries(): Flow<List<SessionSummary>> =
        sessionDao.getAllSessionSummaries().map { list ->
            list.map { raw ->
                SessionSummary(
                    id = raw.id,
                    date = LocalDate.ofEpochDay(raw.dateEpochDay),
                    name = raw.name,
                    exerciseCount = raw.exerciseCount,
                    durationSeconds = if (raw.startedAtEpochSecond > 0 && raw.endedAtEpochSecond > raw.startedAtEpochSecond)
                        raw.endedAtEpochSecond - raw.startedAtEpochSecond else null,
                    notes = raw.notes
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

    fun getVolumeForExercise(exerciseName: String): Flow<List<VolumePoint>> =
        setDao.getVolumePerSessionForExercise(exerciseName).map { list ->
            list.map { VolumePoint(date = LocalDate.ofEpochDay(it.dateEpochDay), totalVolume = it.totalVolume) }
        }

    suspend fun getLastBestSetForExercise(exerciseName: String, currentSessionId: Long): Pair<Float, Int>? =
        setDao.getLastBestSetForExercise(exerciseName, currentSessionId)?.let { it.weightKg to it.reps }

    suspend fun getAllTimeBestForExercise(exerciseName: String): Pair<Float, Int>? =
        setDao.getAllTimeBestForExercise(exerciseName)?.let { it.weightKg to it.reps }

    suspend fun setSessionStarted(sessionId: Long, epochSecond: Long) =
        sessionDao.setStartedAt(sessionId, epochSecond)

    suspend fun setSessionEnded(sessionId: Long, epochSecond: Long) =
        sessionDao.setEndedAt(sessionId, epochSecond)

    suspend fun saveSessionName(sessionId: Long, name: String) =
        sessionDao.updateName(sessionId, name)

    suspend fun saveNotes(sessionId: Long, notes: String) =
        sessionDao.updateNotes(sessionId, notes)

    fun getAllTemplates(): Flow<List<WorkoutTemplate>> =
        templateDao.getAllTemplates().map { list ->
            list.map { WorkoutTemplate(id = it.id, name = it.name, exerciseCount = it.exerciseCount) }
        }

    suspend fun saveAsTemplate(name: String, exerciseNames: List<String>) {
        val templateId = templateDao.insertTemplate(
            TemplateEntity(name = name, createdAtEpochDay = java.time.LocalDate.now().toEpochDay())
        )
        templateDao.insertExercises(
            exerciseNames.mapIndexed { index, exerciseName ->
                TemplateExerciseEntity(templateId = templateId, name = exerciseName, orderIndex = index)
            }
        )
    }

    suspend fun deleteTemplate(templateId: Long) =
        templateDao.deleteTemplateById(templateId)

    suspend fun getTemplateExerciseNames(templateId: Long): List<String> =
        templateDao.getExercisesForTemplate(templateId).map { it.name }
}

private fun SessionEntity.toModel() = WorkoutSession(id = id, date = LocalDate.ofEpochDay(dateEpochDay), name = name, notes = notes)
private fun WorkoutSession.toEntity() = SessionEntity(id = id, dateEpochDay = date.toEpochDay(), name = name, notes = notes)
private fun ExerciseEntity.toModel() = ExerciseEntry(id = id, sessionId = sessionId, name = name, orderIndex = orderIndex)
private fun SetEntity.toModel() = SetEntry(id = id, exerciseId = exerciseId, setNumber = setNumber, weightKg = weightKg, reps = reps)
