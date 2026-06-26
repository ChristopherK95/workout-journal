package com.workoutjournal.ui.screens.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.workoutjournal.data.repository.WorkoutRepository
import com.workoutjournal.domain.model.WorkoutSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

data class SetUi(
    val id: Long,
    val setNumber: Int,
    val weightKg: Float,
    val reps: Int
)

data class ExerciseUi(
    val id: Long,
    val name: String,
    val sets: List<SetUi>,
    val previousBest: Pair<Float, Int>? = null
)

data class SessionUiState(
    val sessionId: Long = 0L,
    val date: LocalDate = LocalDate.now(),
    val name: String = "",
    val exercises: List<ExerciseUi> = emptyList(),
    val sessionDeleted: Boolean = false
)

class SessionViewModel(
    private val repository: WorkoutRepository,
    private val sessionId: Long
) : ViewModel() {

    private val _uiState = MutableStateFlow(SessionUiState(sessionId = sessionId))
    val uiState: StateFlow<SessionUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getSessionFlow(sessionId).collect { session ->
                session?.let {
                    _uiState.update { state -> state.copy(date = it.date, name = it.name) }
                }
            }
        }
        viewModelScope.launch {
            repository.getExercisesWithSets(sessionId).collect { exercisesWithSets ->
                val exercisesUi = exercisesWithSets.map { eWithSets ->
                    ExerciseUi(
                        id = eWithSets.exercise.id,
                        name = eWithSets.exercise.name,
                        sets = eWithSets.sets.map { s ->
                            SetUi(id = s.id, setNumber = s.setNumber, weightKg = s.weightKg, reps = s.reps)
                        },
                        previousBest = repository.getLastBestSetForExercise(eWithSets.exercise.name, sessionId)
                    )
                }
                _uiState.update { it.copy(exercises = exercisesUi) }
            }
        }
    }

    fun updateName(name: String) {
        _uiState.update { it.copy(name = name) }
    }

    fun saveSessionName() {
        viewModelScope.launch {
            val state = _uiState.value
            repository.updateSession(WorkoutSession(id = sessionId, date = state.date, name = state.name))
        }
    }

    fun addExercise(name: String) {
        viewModelScope.launch {
            val orderIndex = _uiState.value.exercises.size
            repository.addExercise(sessionId, name.trim(), orderIndex)
        }
    }

    fun deleteExercise(exerciseId: Long) {
        viewModelScope.launch { repository.deleteExercise(exerciseId) }
    }

    fun addSet(exerciseId: Long) {
        viewModelScope.launch {
            val exercise = _uiState.value.exercises.find { it.id == exerciseId }
            val setNumber = (exercise?.sets?.size ?: 0) + 1
            repository.addSet(exerciseId, setNumber, 0f, 0)
        }
    }

    fun updateSet(setId: Long, weightKg: Float, reps: Int) {
        viewModelScope.launch { repository.updateSet(setId, weightKg, reps) }
    }

    fun deleteSet(setId: Long) {
        viewModelScope.launch { repository.deleteSet(setId) }
    }

    fun duplicateSet(setId: Long) {
        viewModelScope.launch {
            val exercise = _uiState.value.exercises.find { e -> e.sets.any { it.id == setId } } ?: return@launch
            val source = exercise.sets.find { it.id == setId } ?: return@launch
            val nextSetNumber = exercise.sets.size + 1
            repository.addSet(exercise.id, nextSetNumber, source.weightKg, source.reps)
        }
    }

    fun deleteSession() {
        viewModelScope.launch {
            val state = _uiState.value
            repository.deleteSession(WorkoutSession(id = sessionId, date = state.date, name = state.name))
            _uiState.update { it.copy(sessionDeleted = true) }
        }
    }

    companion object {
        fun Factory(repository: WorkoutRepository, sessionId: Long): ViewModelProvider.Factory =
            viewModelFactory {
                initializer { SessionViewModel(repository, sessionId) }
            }
    }
}
