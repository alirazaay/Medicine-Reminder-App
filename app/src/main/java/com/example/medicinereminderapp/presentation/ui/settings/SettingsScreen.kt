package com.example.medicinereminderapp.presentation.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.medicinereminderapp.presentation.ui.components.StandardTopAppBar
import com.example.medicinereminderapp.presentation.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit
) {
    val settings by viewModel.settings.collectAsState()

    Scaffold(
        topBar = {
            StandardTopAppBar(
                title = "Settings",
                showBackArrow = true,
                onBackClick = onNavigateBack
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SettingItem(
                title = "Enable Notifications",
                description = "Receive medicine reminders",
                checked = settings.notificationsEnabled,
                onCheckedChange = { viewModel.toggleNotifications(it) }
            )
            HorizontalDivider()
            SettingItem(
                title = "Dark Mode",
                description = "Use dark theme across the app",
                checked = settings.isDarkMode,
                onCheckedChange = { viewModel.toggleDarkMode(it) }
            )
            HorizontalDivider()
            SettingItem(
                title = "Reminder Sound",
                description = "Play sound when reminder fires",
                checked = settings.soundEnabled,
                onCheckedChange = { viewModel.toggleSound(it) }
            )
            HorizontalDivider()
            SettingItem(
                title = "Vibration",
                description = "Vibrate when reminder fires",
                checked = settings.vibrationEnabled,
                onCheckedChange = { viewModel.toggleVibration(it) }
            )
        }
    }
}

@Composable
fun SettingItem(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
