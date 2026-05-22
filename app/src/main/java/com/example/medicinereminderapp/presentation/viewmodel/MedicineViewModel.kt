package com.example.medicinereminderapp.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medicinereminderapp.data.local.entity.MedicineEntity
import com.example.medicinereminderapp.domain.repository.MedicineRepository
import com.example.medicinereminderapp.domain.scheduler.ReminderScheduler
import com.example.medicinereminderapp.presentation.event.UiAction
import com.example.medicinereminderapp.presentation.event.UiEvent
import com.example.medicinereminderapp.presentation.state.MedicineUiState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MedicineViewModel(
    private val repository: MedicineRepository,
    private val reminderScheduler: ReminderScheduler
) : ViewModel() {

    private val _uiState = MutableStateFlow(MedicineUiState())
    val uiState: StateFlow<MedicineUiState> = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent: SharedFlow<UiEvent> = _uiEvent.asSharedFlow()

    init {
        loadAllMedicines()
    }

    fun onAction(action: UiAction) {
        when (action) {
            is UiAction.AddMedicine -> addMedicine(action.medicine)
            is UiAction.UpdateMedicine -> updateMedicine(action.medicine)
            is UiAction.DeleteMedicine -> deleteMedicine(action.medicine)
            is UiAction.ToggleMedicineActiveStatus -> toggleActiveStatus(action.medicine)
            else -> Unit
        }
    }

    private fun loadAllMedicines() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            repository.getAllMedicines()
                .catch { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message ?: "An error occurred") }
                }
                .collect { medicines ->
                    _uiState.update { it.copy(medicines = medicines, isLoading = false) }
                }
        }
    }

    fun loadMedicineById(id: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val medicine = repository.getMedicineById(id)
                _uiState.update { it.copy(selectedMedicine = medicine, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "An error occurred") }
            }
        }
    }

    private fun addMedicine(medicine: MedicineEntity) {
        if (!validateMedicine(medicine)) return
        viewModelScope.launch {
            try {
                val id = repository.insertMedicine(medicine)
                reminderScheduler.scheduleReminder(medicine.copy(id = id))
                _uiEvent.emit(UiEvent.ShowSnackbar("Medicine added successfully"))
                _uiEvent.emit(UiEvent.NavigateBack)
            } catch (e: Exception) {
                _uiEvent.emit(UiEvent.ShowSnackbar("Failed to add medicine"))
            }
        }
    }

    private fun updateMedicine(medicine: MedicineEntity) {
        if (!validateMedicine(medicine)) return
        viewModelScope.launch {
            try {
                repository.updateMedicine(medicine)
                reminderScheduler.scheduleReminder(medicine)
                _uiEvent.emit(UiEvent.ShowSnackbar("Medicine updated successfully"))
                _uiEvent.emit(UiEvent.NavigateBack)
            } catch (e: Exception) {
                _uiEvent.emit(UiEvent.ShowSnackbar("Failed to update medicine"))
            }
        }
    }

    private fun deleteMedicine(medicine: MedicineEntity) {
        viewModelScope.launch {
            try {
                repository.deleteMedicine(medicine)
                reminderScheduler.cancelReminder(medicine.id)
                _uiEvent.emit(UiEvent.ShowSnackbar("Medicine deleted"))
            } catch (e: Exception) {
                _uiEvent.emit(UiEvent.ShowSnackbar("Failed to delete medicine"))
            }
        }
    }

    private fun toggleActiveStatus(medicine: MedicineEntity) {
        viewModelScope.launch {
            try {
                val updated = medicine.copy(isActive = !medicine.isActive)
                repository.updateMedicine(updated)
                if (updated.isActive) {
                    reminderScheduler.scheduleReminder(updated)
                } else {
                    reminderScheduler.cancelReminder(updated.id)
                }
            } catch (e: Exception) {
                _uiEvent.emit(UiEvent.ShowSnackbar("Failed to update status"))
            }
        }
    }

    private fun validateMedicine(medicine: MedicineEntity): Boolean {
        if (medicine.name.isBlank()) {
            viewModelScope.launch { _uiEvent.emit(UiEvent.ShowSnackbar("Name cannot be empty")) }
            return false
        }
        if (medicine.dosage.isBlank()) {
            viewModelScope.launch { _uiEvent.emit(UiEvent.ShowSnackbar("Dosage cannot be empty")) }
            return false
        }
        if (medicine.reminderTimes.isEmpty()) {
            viewModelScope.launch { _uiEvent.emit(UiEvent.ShowSnackbar("Add at least one reminder time")) }
            return false
        }
        return true
    }
}
