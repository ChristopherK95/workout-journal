package com.workoutjournal.ui.screens.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.workoutjournal.data.repository.WorkoutRepository
import com.workoutjournal.domain.model.ProgressPoint
import com.workoutjournal.domain.model.VolumePoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ProgressUiState(
    val exerciseNames: List<String> = emptyList(),
    val selectedExercise: String = "",
    val progressPoints: List<ProgressPoint> = emptyList(),
    val volumePoints: List<VolumePoint> = emptyList(),
    val personalBestKg: Float? = null,
    val lastWeightKg: Float? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
class ProgressViewModel(private val repository: WorkoutRepository) : ViewModel() {

    private val _selectedExercise = MutableStateFlow("")
    private val _uiState = MutableStateFlow(ProgressUiState())
    val uiState: StateFlow<ProgressUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getAllExerciseNames().collect { names ->
                val current = _uiState.value.selectedExercise
                val newSelected = if (current.isEmpty() && names.isNotEmpty()) names.first() else current
                _uiState.update { it.copy(exerciseNames = names) }
                if (newSelected != current) selectExercise(newSelected)
            }
        }
        viewModelScope.launch {
            _selectedExercise
                .filter { it.isNotEmpty() }
                .flatMapLatest { name -> repository.getProgressForExercise(name) }
                .collect { points ->
                    _uiState.update { state ->
                        state.copy(
                            progressPoints = points,
                            personalBestKg = points.maxOfOrNull { it.maxWeightKg },
                            lastWeightKg = points.lastOrNull()?.maxWeightKg
                        )
                    }
                }
        }
        viewModelScope.launch {
            _selectedExercise
                .filter { it.isNotEmpty() }
                .flatMapLatest { name -> repository.getVolumeForExercise(name) }
                .collect { points ->
                    _uiState.update { it.copy(volumePoints = points) }
                }
        }
    }

    fun selectExercise(name: String) {
        _selectedExercise.value = name
        _uiState.update { it.copy(selectedExercise = name) }
    }

    companion object {
        fun Factory(repository: WorkoutRepository): ViewModelProvider.Factory = viewModelFactory {
            initializer { ProgressViewModel(repository) }
        }
    }
}
