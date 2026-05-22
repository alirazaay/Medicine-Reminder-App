package com.example.medicinereminderapp.ui.screen.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.medicinereminderapp.data.local.preferences.UserPreferences
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val preferences = UserPreferences(application)

    val userName: StateFlow<String> = preferences.userNameFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "User")

    val notificationsEnabled: StateFlow<Boolean> = preferences.notificationsEnabledFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val snoozeEnabled: StateFlow<Boolean> = preferences.snoozeEnabledFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val themeMode: StateFlow<String> = preferences.themeModeFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "SYSTEM")

    fun saveUserName(name: String) {
        viewModelScope.launch {
            preferences.saveUserName(name)
        }
    }

    fun saveNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferences.saveNotificationsEnabled(enabled)
        }
    }

    fun saveSnoozeEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferences.saveSnoozeEnabled(enabled)
        }
    }

    fun saveThemeMode(mode: String) {
        viewModelScope.launch {
            preferences.saveThemeMode(mode)
        }
    }
}
