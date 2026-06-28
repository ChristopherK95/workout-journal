package com.workoutjournal.ui.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.workoutjournal.data.repository.WorkoutRepository
import com.workoutjournal.domain.model.SessionSummary
import com.workoutjournal.domain.model.WorkoutTemplate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

data class HistoryUiState(
    val sessions: List<SessionSummary> = emptyList(),
    val isLoading: Boolean = true,
    val navigateToSessionId: Long? = null,
    val currentStreak: Int = 0,
    val templates: List<WorkoutTemplate> = emptyList()
)

class HistoryViewModel(private val repository: WorkoutRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getAllSessionSummaries().collect { sessions ->
                _uiState.update {
                    it.copy(
                        sessions = sessions,
                        isLoading = false,
                        currentStreak = calculateStreak(sessions.map { s -> s.date })
                    )
                }
            }
        }
        viewModelScope.launch {
            repository.getAllTemplates().collect { templates ->
                _uiState.update { it.copy(templates = templates) }
            }
        }
    }

    fun createNewSession(date: LocalDate) {
        viewModelScope.launch {
            val id = repository.createSession(date)
            _uiState.update { it.copy(navigateToSessionId = id) }
        }
    }

    fun createSessionFromTemplate(date: LocalDate, templateId: Long) {
        viewModelScope.launch {
            val sessionId = repository.createSession(date)
            val exerciseNames = repository.getTemplateExerciseNames(templateId)
            exerciseNames.forEachIndexed { index, name ->
                repository.addExercise(sessionId, name, index)
            }
            _uiState.update { it.copy(navigateToSessionId = sessionId) }
        }
    }

    fun deleteTemplate(templateId: Long) {
        viewModelScope.launch { repository.deleteTemplate(templateId) }
    }

    fun onNavigatedToSession() {
        _uiState.update { it.copy(navigateToSessionId = null) }
    }

    companion object {
        fun Factory(repository: WorkoutRepository): ViewModelProvider.Factory = viewModelFactory {
            initializer { HistoryViewModel(repository) }
        }
    }
}

private fun calculateStreak(sessionDates: List<LocalDate>): Int {
    if (sessionDates.isEmpty()) return 0
    val dates = sessionDates.toSet()
    val today = LocalDate.now()

    fun weekStart(d: LocalDate): LocalDate =
        d.minusDays((d.dayOfWeek.value - 1).toLong())

    fun weekHasSession(ws: LocalDate): Boolean =
        (0..6).any { dates.contains(ws.plusDays(it.toLong())) }

    var week = weekStart(today)
    if (!weekHasSession(week)) week = week.minusWeeks(1)

    var streak = 0
    while (weekHasSession(week)) {
        streak++
        week = week.minusWeeks(1)
    }
    return streak
}
