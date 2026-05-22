package com.example.medicinereminderapp.presentation.state

import com.example.medicinereminderapp.data.local.entity.MedicineEntity

data class MedicineUiState(
    val medicines: List<MedicineEntity> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedMedicine: MedicineEntity? = null
)
