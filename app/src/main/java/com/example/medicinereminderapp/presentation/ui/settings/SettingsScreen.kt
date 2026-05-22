package com.example.medicinereminderapp.presentation.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.medicinereminderapp.presentation.viewmodel.SettingsViewModel
import com.example.medicinereminderapp.ui.theme.LightPrimary

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateToHome: () -> Unit = {},
    onNavigateToCalendar: () -> Unit = {},
    onNavigateToHistory: () -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    val settings by viewModel.settings.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToHome,
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToCalendar,
                    icon = { Icon(Icons.Default.Schedule, contentDescription = "Calendar") },
                    label = { Text("Calendar") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToHistory,
                    icon = { Icon(Icons.Default.History, contentDescription = "History") },
                    label = { Text("History") }
                )
                NavigationBarItem(
                    selected = true,
                    onClick = { },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                    label = { Text("Settings") }
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "MedTrack",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Settings",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            SettingsSection(title = "NOTIFICATIONS") {
                ToggleRow(
                    icon = Icons.Default.Notifications,
                    label = "Notifications",
                    checked = settings.notificationsEnabled,
                    onCheckedChange = { viewModel.toggleNotifications(it) }
                )
                DividerRow()
                ToggleRow(
                    icon = Icons.Default.VolumeUp,
                    label = "Sound",
                    checked = settings.soundEnabled,
                    onCheckedChange = { viewModel.toggleSound(it) }
                )
                DividerRow()
                ToggleRow(
                    icon = Icons.Default.PhoneAndroid,
                    label = "Vibration",
                    checked = settings.vibrationEnabled,
                    onCheckedChange = { viewModel.toggleVibration(it) }
                )
                DividerRow()
                NavigationRow(
                    icon = Icons.Default.Schedule,
                    label = "Snooze Duration",
                    value = "10 mins",
                    onClick = onNavigateBack
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            SettingsSection(title = "DISPLAY") {
                ToggleRow(
                    icon = Icons.Default.DarkMode,
                    label = "Dark Theme",
                    checked = settings.isDarkMode,
                    onCheckedChange = { viewModel.toggleDarkMode(it) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            SettingsSection(title = "DATA") {
                NavigationRow(
                    icon = Icons.Default.CloudUpload,
                    label = "Backup / Restore",
                    value = "",
                    onClick = onNavigateBack
                )
                DividerRow()
                NavigationRow(
                    icon = Icons.Default.FileDownload,
                    label = "Export CSV",
                    value = "",
                    onClick = onNavigateBack
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            SettingsSection(title = "SUPPORT") {
                NavigationRow(
                    icon = Icons.Default.HelpOutline,
                    label = "Help Center",
                    value = "",
                    onClick = onNavigateBack
                )
                DividerRow()
                NavigationRow(
                    icon = Icons.Default.Info,
                    label = "About MedTrack",
                    value = "",
                    onClick = onNavigateBack
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(1.dp, RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = LightPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
private fun ToggleRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = label, fontSize = 15.sp)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun NavigationRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = label, fontSize = 15.sp)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (value.isNotBlank()) {
                Text(text = value, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                Spacer(modifier = Modifier.width(6.dp))
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null)
        }
    }
}

@Composable
private fun DividerRow() {
    HorizontalDivider(
        modifier = Modifier.padding(vertical = 10.dp),
        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
    )
}