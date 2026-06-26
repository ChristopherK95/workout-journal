package com.workoutjournal.domain.model

import java.time.LocalDate

data class WorkoutSession(
    val id: Long = 0,
    val date: LocalDate,
    val name: String = ""
)

data class ExerciseEntry(
    val id: Long = 0,
    val sessionId: Long,
    val name: String,
    val orderIndex: Int = 0
)

data class SetEntry(
    val id: Long = 0,
    val exerciseId: Long,
    val setNumber: Int,
    val weightKg: Float,
    val reps: Int
)

data class ExerciseWithSets(
    val exercise: ExerciseEntry,
    val sets: List<SetEntry>
)

data class SessionSummary(
    val id: Long,
    val date: LocalDate,
    val name: String,
    val exerciseCount: Int
)

data class ProgressPoint(
    val date: LocalDate,
    val maxWeightKg: Float
)
