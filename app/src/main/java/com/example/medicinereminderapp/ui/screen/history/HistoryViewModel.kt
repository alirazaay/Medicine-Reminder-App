package com.example.medicinereminderapp.ui.screen.history

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.medicinereminderapp.data.local.AppDatabase
import com.example.medicinereminderapp.data.local.entity.ReminderLogEntity
import com.example.medicinereminderapp.data.repository.MedicineRepositoryImpl
import com.example.medicinereminderapp.domain.model.LogStatus
import com.example.medicinereminderapp.domain.repository.MedicineRepository
import kotlinx.coroutines.flow.*

class HistoryViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val repository: MedicineRepository = MedicineRepositoryImpl(database.medicineDao, database.reminderLogDao)

    val historyLogs: StateFlow<List<ReminderLogEntity>> = repository.getAllReminderLogs()
        .map { logs ->
            // Filter out pending logs because history only shows resolved actions (Taken, Skipped, Missed)
            logs.filter { it.status != LogStatus.PENDING }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val adherencePercentage: StateFlow<Float> = historyLogs.map { logs ->
        if (logs.isEmpty()) 1f
        else {
            val taken = logs.count { it.status == LogStatus.TAKEN }
            taken.toFloat() / logs.size
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1f)
}
