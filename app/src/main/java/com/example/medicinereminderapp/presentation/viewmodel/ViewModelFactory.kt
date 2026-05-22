package com.example.medicinereminderapp.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.medicinereminderapp.domain.repository.MedicineRepository

class ViewModelFactory(
    private val repository: MedicineRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(MedicineViewModel::class.java) -> {
                MedicineViewModel(repository) as T
            }
            modelClass.isAssignableFrom(ReminderLogViewModel::class.java) -> {
                ReminderLogViewModel(repository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
