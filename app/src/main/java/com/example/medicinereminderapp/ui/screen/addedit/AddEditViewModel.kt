package com.example.medicinereminderapp.ui.screen.addedit

import android.app.Application
import android.util.Log
import androidx.compose.runtime.*
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.medicinereminderapp.data.local.db.AppDatabase
import com.example.medicinereminderapp.data.model.Medicine
import com.example.medicinereminderapp.data.model.MedicineType
import com.example.medicinereminderapp.data.repository.MedicineRepository
import com.example.medicinereminderapp.data.repository.MedicineRepositoryImpl
import com.example.medicinereminderapp.util.AlarmScheduler
import com.example.medicinereminderapp.util.DateTimeUtils
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class AddEditViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG = "AddEditViewModel"
    private val database = AppDatabase.getDatabase(application)
    private val repository: MedicineRepository = MedicineRepositoryImpl(database.medicineDao(), database.reminderLogDao())

    var medicineId by mutableStateOf(-1L)
        private set

    // Form Fields
    var name by mutableStateOf("")
    var dosage by mutableStateOf("")
    var type by mutableStateOf(MedicineType.TABLET)
    var instructions by mutableStateOf("")
    var frequency by mutableStateOf("DAILY") // "DAILY", "SPECIFIC_DAYS", "INTERVAL_DAYS"
    var selectedDays = mutableStateListOf<String>() // e.g. ["monday", "wednesday", "friday"]
    var intervalDays by mutableStateOf("1")
    var startDate by mutableStateOf(DateTimeUtils.getTodayMillis())
    var endDate by mutableStateOf<Long?>(null)
    var hasEndDate by mutableStateOf(false)
    var reminderTimes = mutableStateListOf<String>()

    private val _isSaved = MutableSharedFlow<Boolean>()
    val isSaved: SharedFlow<Boolean> = _isSaved.asSharedFlow()

    fun loadMedicine(id: Long) {
        if (id == -1L || id == medicineId) return
        medicineId = id
        viewModelScope.launch {
            repository.getMedicineById(id)?.let { medicine ->
                name = medicine.name
                dosage = medicine.dosage
                type = medicine.type
                instructions = medicine.instructions
                frequency = medicine.frequency
                
                selectedDays.clear()
                if (medicine.frequency == "SPECIFIC_DAYS") {
                    medicine.frequencyPattern?.split(",")?.forEach {
                        selectedDays.add(it.trim().lowercase())
                    }
                }
                
                if (medicine.frequency == "INTERVAL_DAYS") {
                    intervalDays = medicine.frequencyPattern ?: "1"
                }

                startDate = medicine.startDate
                endDate = medicine.endDate
                hasEndDate = medicine.endDate != null
                
                reminderTimes.clear()
                reminderTimes.addAll(medicine.reminderTimes)
            }
        }
    }

    fun addTime(timeStr: String) {
        if (!reminderTimes.contains(timeStr)) {
            reminderTimes.add(timeStr)
            reminderTimes.sort()
        }
    }

    fun removeTime(timeStr: String) {
        reminderTimes.remove(timeStr)
    }

    fun toggleDay(day: String) {
        val lowerDay = day.lowercase()
        if (selectedDays.contains(lowerDay)) {
            selectedDays.remove(lowerDay)
        } else {
            selectedDays.add(lowerDay)
        }
    }

    fun saveMedicine() {
        if (name.isBlank() || dosage.isBlank() || reminderTimes.isEmpty()) {
            return
        }

        val pattern = when (frequency) {
            "SPECIFIC_DAYS" -> selectedDays.joinToString(",")
            "INTERVAL_DAYS" -> intervalDays
            else -> null
        }

        viewModelScope.launch {
            try {
                // If editing, cancel existing alarms first
                if (medicineId != -1L) {
                    repository.getMedicineById(medicineId)?.let { oldMed ->
                        AlarmScheduler.cancelAlarmsForMedicine(getApplication(), oldMed)
                    }
                }

                val medicine = Medicine(
                    id = if (medicineId == -1L) 0L else medicineId,
                    name = name.trim(),
                    dosage = dosage.trim(),
                    type = type,
                    instructions = instructions.trim(),
                    startDate = startDate,
                    endDate = if (hasEndDate) endDate else null,
                    frequency = frequency,
                    frequencyPattern = pattern,
                    reminderTimes = reminderTimes.toList(),
                    isActive = true
                )

                // Write to database
                val newId = repository.insertMedicine(medicine)
                val savedMedicine = medicine.copy(id = if (medicineId == -1L) newId else medicineId)

                // Register alarms via system AlarmManager
                AlarmScheduler.scheduleAlarmsForMedicine(getApplication(), savedMedicine)
                
                Log.d(TAG, "Saved medicine successfully. ID: ${savedMedicine.id}, Name: ${savedMedicine.name}")
                _isSaved.emit(true)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to save medicine", e)
            }
        }
    }
}
