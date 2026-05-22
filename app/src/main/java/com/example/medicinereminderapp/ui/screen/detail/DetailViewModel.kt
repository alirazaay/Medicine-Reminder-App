package com.example.medicinereminderapp.ui.screen.detail

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.medicinereminderapp.data.local.db.AppDatabase
import com.example.medicinereminderapp.data.model.LogStatus
import com.example.medicinereminderapp.data.model.Medicine
import com.example.medicinereminderapp.data.repository.MedicineRepository
import com.example.medicinereminderapp.data.repository.MedicineRepositoryImpl
import com.example.medicinereminderapp.util.AlarmScheduler
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class DetailViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val repository: MedicineRepository = MedicineRepositoryImpl(database.medicineDao(), database.reminderLogDao())

    private val _medicineId = MutableStateFlow(-1L)

    val medicine: StateFlow<Medicine?> = _medicineId
        .flatMapLatest { id ->
            if (id == -1L) flowOf(null)
            else repository.getMedicineByIdFlow(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val adherenceStats: StateFlow<Pair<Int, Int>> = _medicineId
        .flatMapLatest { id ->
            if (id == -1L) flowOf(Pair(0, 0))
            else repository.getLogsForMedicine(id).map { logs ->
                val total = logs.count { it.status != LogStatus.PENDING }
                val taken = logs.count { it.status == LogStatus.TAKEN }
                Pair(taken, total)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Pair(0, 0))

    fun setMedicineId(id: Long) {
        _medicineId.value = id
    }

    fun toggleMedicineActive(medicine: Medicine) {
        val updated = medicine.copy(isActive = !medicine.isActive)
        viewModelScope.launch {
            repository.updateMedicine(updated)
            if (updated.isActive) {
                AlarmScheduler.scheduleAlarmsForMedicine(getApplication(), updated)
            } else {
                AlarmScheduler.cancelAlarmsForMedicine(getApplication(), updated)
            }
        }
    }

    fun deleteMedicine(medicine: Medicine) {
        viewModelScope.launch {
            // Cancel system alarms first
            AlarmScheduler.cancelAlarmsForMedicine(getApplication(), medicine)
            // Delete from database (Room cascades logs)
            repository.deleteMedicine(medicine)
        }
    }
}
