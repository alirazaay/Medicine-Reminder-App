package com.example.medicinereminderapp.presentation.state

import com.example.medicinereminderapp.data.local.entity.ReminderLogEntity

data class ReminderLogUiState(
    val logs: List<ReminderLogEntity> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
