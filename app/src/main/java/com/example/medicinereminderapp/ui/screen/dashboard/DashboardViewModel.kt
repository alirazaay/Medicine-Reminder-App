package com.example.medicinereminderapp.ui.screen.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.medicinereminderapp.data.local.AppDatabase
import com.example.medicinereminderapp.data.local.entity.ReminderLogEntity
import com.example.medicinereminderapp.data.local.preferences.UserPreferences
import com.example.medicinereminderapp.data.repository.MedicineRepositoryImpl
import com.example.medicinereminderapp.domain.model.LogStatus
import com.example.medicinereminderapp.domain.model.MedicineType
import com.example.medicinereminderapp.domain.repository.MedicineRepository
import com.example.medicinereminderapp.util.DateTimeUtils
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ReminderItem(
    val medicineId: Long,
    val name: String,
    val dosage: String,
    val type: MedicineType,
    val instructions: String,
    val scheduledTime: String,
    val scheduledDateTimeMs: Long,
    val status: LogStatus,
    val logId: Long = 0
)

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val repository: MedicineRepository = MedicineRepositoryImpl(database.medicineDao, database.reminderLogDao)
    private val preferences = UserPreferences(application)

    private val _selectedDate = MutableStateFlow(DateTimeUtils.getTodayMillis())
    val selectedDate: StateFlow<Long> = _selectedDate.asStateFlow()

    val userName: StateFlow<String> = preferences.userNameFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "User")

    @OptIn(ExperimentalCoroutinesApi::class)
    val reminderItems: StateFlow<List<ReminderItem>> = _selectedDate
        .flatMapLatest { date ->
            val startOfDay = DateTimeUtils.getStartOfDay(date)
            val endOfDay = DateTimeUtils.getEndOfDay(date)
            
            combine(
                repository.getActiveMedicines(),
                repository.getReminderLogsForDay(startOfDay, endOfDay)
            ) { medicines, logs ->
                val items = mutableListOf<ReminderItem>()
                
                medicines.forEach { medicine ->
                    if (DateTimeUtils.isScheduledOnDate(medicine, date)) {
                        medicine.reminderTimes.forEach { timeStr ->
                            val scheduledTimeMs = DateTimeUtils.getTimeInMillis(date, timeStr)
                            
                            val matchingLog = logs.find { it.medicineId == medicine.id && it.scheduledDateTime == scheduledTimeMs }
                            
                            items.add(
                                ReminderItem(
                                    medicineId = medicine.id,
                                    name = medicine.name,
                                    dosage = medicine.dosage,
                                    type = medicine.type,
                                    instructions = medicine.instructions,
                                    scheduledTime = timeStr,
                                    scheduledDateTimeMs = scheduledTimeMs,
                                    status = matchingLog?.status ?: LogStatus.PENDING,
                                    logId = matchingLog?.id ?: 0L
                                )
                            )
                        }
                    }
                }
                
                items.sortBy { it.scheduledTime }
                items
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val adherenceProgress: StateFlow<Float> = reminderItems.map { items ->
        if (items.isEmpty()) 0f
        else {
            val taken = items.count { it.status == LogStatus.TAKEN }
            taken.toFloat() / items.size
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0f)

    fun selectDate(dateMillis: Long) {
        _selectedDate.value = dateMillis
    }

    fun updateReminderStatus(item: ReminderItem, status: LogStatus) {
        viewModelScope.launch {
            if (item.logId != 0L) {
                val log = ReminderLogEntity(
                    id = item.logId,
                    medicineId = item.medicineId,
                    medicineName = item.name,
                    dosage = item.dosage,
                    scheduledDateTime = item.scheduledDateTimeMs,
                    status = status,
                    actionDateTime = System.currentTimeMillis()
                )
                repository.updateReminderLog(log)
            } else {
                val log = ReminderLogEntity(
                    medicineId = item.medicineId,
                    medicineName = item.name,
                    dosage = item.dosage,
                    scheduledDateTime = item.scheduledDateTimeMs,
                    status = status,
                    actionDateTime = System.currentTimeMillis()
                )
                repository.insertReminderLog(log)
            }
        }
    }
}
