package com.example.medicinereminderapp.domain.model

data class AppSettings(
    val notificationsEnabled: Boolean = true,
    val isDarkMode: Boolean = false,
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true
)
