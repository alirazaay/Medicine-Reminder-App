package com.example.medicinereminderapp.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.medicinereminderapp.domain.repository.MedicineRepository
import com.example.medicinereminderapp.domain.scheduler.ReminderScheduler

class ViewModelFactory(
    private val repository: MedicineRepository,
    private val reminderScheduler: ReminderScheduler
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(MedicineViewModel::class.java) -> {
                MedicineViewModel(repository, reminderScheduler) as T
            }
            modelClass.isAssignableFrom(ReminderLogViewModel::class.java) -> {
                ReminderLogViewModel(repository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
