package com.example.medicinereminderapp.domain.repository

import com.example.medicinereminderapp.domain.model.AppSettings
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun getSettings(): Flow<AppSettings>
    suspend fun updateNotificationsEnabled(enabled: Boolean)
    suspend fun updateDarkMode(enabled: Boolean)
    suspend fun updateSoundEnabled(enabled: Boolean)
    suspend fun updateVibrationEnabled(enabled: Boolean)
}
