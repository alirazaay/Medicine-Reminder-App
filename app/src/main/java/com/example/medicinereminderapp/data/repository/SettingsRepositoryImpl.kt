package com.example.medicinereminderapp.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.example.medicinereminderapp.domain.model.AppSettings
import com.example.medicinereminderapp.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsRepositoryImpl(
    private val dataStore: DataStore<Preferences>
) : SettingsRepository {

    private object PreferencesKeys {
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val IS_DARK_MODE = booleanPreferencesKey("is_dark_mode")
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val VIBRATION_ENABLED = booleanPreferencesKey("vibration_enabled")
    }

    override fun getSettings(): Flow<AppSettings> {
        return dataStore.data.map { preferences ->
            AppSettings(
                notificationsEnabled = preferences[PreferencesKeys.NOTIFICATIONS_ENABLED] ?: true,
                isDarkMode = preferences[PreferencesKeys.IS_DARK_MODE] ?: false,
                soundEnabled = preferences[PreferencesKeys.SOUND_ENABLED] ?: true,
                vibrationEnabled = preferences[PreferencesKeys.VIBRATION_ENABLED] ?: true
            )
        }
    }

    override suspend fun updateNotificationsEnabled(enabled: Boolean) {
        dataStore.edit { it[PreferencesKeys.NOTIFICATIONS_ENABLED] = enabled }
    }

    override suspend fun updateDarkMode(enabled: Boolean) {
        dataStore.edit { it[PreferencesKeys.IS_DARK_MODE] = enabled }
    }

    override suspend fun updateSoundEnabled(enabled: Boolean) {
        dataStore.edit { it[PreferencesKeys.SOUND_ENABLED] = enabled }
    }

    override suspend fun updateVibrationEnabled(enabled: Boolean) {
        dataStore.edit { it[PreferencesKeys.VIBRATION_ENABLED] = enabled }
    }
}
