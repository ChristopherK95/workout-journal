package com.workoutjournal.ui.components

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class TimerViewModel : ViewModel() {
    var seconds by mutableStateOf(0)
        private set
    var running by mutableStateOf(false)
        private set
    var dialogOpen by mutableStateOf(false)
        private set

    private var job: Job? = null

    fun toggle() { if (running) pause() else start() }

    fun reset() { pause(); seconds = 0 }

    fun openDialog() { dialogOpen = true }

    fun closeDialog() { dialogOpen = false }

    private fun start() {
        running = true
        job = viewModelScope.launch {
            while (true) { delay(1000L); seconds++ }
        }
    }

    private fun pause() {
        running = false
        job?.cancel()
        job = null
    }
}
