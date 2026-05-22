package com.example.medicinereminderapp.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medicinereminderapp.domain.model.LogStatus
import com.example.medicinereminderapp.domain.repository.MedicineRepository
import com.example.medicinereminderapp.presentation.event.UiAction
import com.example.medicinereminderapp.presentation.event.UiEvent
import com.example.medicinereminderapp.presentation.state.ReminderLogUiState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ReminderLogViewModel(
    private val repository: MedicineRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReminderLogUiState())
    val uiState: StateFlow<ReminderLogUiState> = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent: SharedFlow<UiEvent> = _uiEvent.asSharedFlow()

    init {
        loadAllLogs()
    }

    fun onAction(action: UiAction) {
        when (action) {
            is UiAction.UpdateLogStatus -> updateLogStatus(action.logId, action.status, action.actionTime)
            else -> Unit
        }
    }

    fun loadAllLogs() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            repository.getAllReminderLogs()
                .catch { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message ?: "An error occurred") }
                }
                .collect { logs ->
                    _uiState.update { it.copy(logs = logs, isLoading = false) }
                }
        }
    }

    // Intentionally keep a single source of truth for logs to avoid
    // screens overwriting each other's data.

    private fun updateLogStatus(logId: Long, status: LogStatus, actionTime: Long) {
        viewModelScope.launch {
            try {
                repository.updateLogStatus(logId, status, actionTime)
                _uiEvent.emit(UiEvent.ShowSnackbar("Status updated to ${status.name}"))
            } catch (e: Exception) {
                _uiEvent.emit(UiEvent.ShowSnackbar("Failed to update status"))
            }
        }
    }
}
