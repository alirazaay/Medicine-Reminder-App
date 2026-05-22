package com.example.medicinereminderapp.presentation.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.medicinereminderapp.domain.model.LogStatus
import com.example.medicinereminderapp.presentation.ui.components.EmptyStateView
import com.example.medicinereminderapp.presentation.ui.components.MedicineCard
import com.example.medicinereminderapp.presentation.ui.components.StandardTopAppBar
import com.example.medicinereminderapp.presentation.viewmodel.MedicineViewModel
import com.example.medicinereminderapp.presentation.viewmodel.ReminderLogViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.runtime.remember

data class TodayReminder(
    val medicineId: Long,
    val name: String,
    val dosage: String,
    val time: String,
    val status: LogStatus?
)

@Composable
fun HomeScreen(
    medicineViewModel: MedicineViewModel,
    reminderLogViewModel: ReminderLogViewModel,
    onNavigateToAddEdit: () -> Unit,
    onNavigateToDetail: (Long) -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val medState by medicineViewModel.uiState.collectAsState()
    val logState by reminderLogViewModel.uiState.collectAsState()

    val todayReminders = remember(medState.medicines, logState.logs) {
        val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val reminders = mutableListOf<TodayReminder>()
        
        medState.medicines.filter { it.isActive }.forEach { medicine ->
            medicine.reminderTimes.forEach { timeStr ->
                val logForTime = logState.logs.find { log ->
                    log.medicineId == medicine.id && dateFormat.format(Date(log.scheduledDateTime)) == timeStr
                }
                reminders.add(
                    TodayReminder(
                        medicineId = medicine.id,
                        name = medicine.name,
                        dosage = medicine.dosage,
                        time = timeStr,
                        status = logForTime?.status
                    )
                )
            }
        }
        reminders.sortBy { it.time }
        reminders
    }

    Scaffold(
        topBar = {
            StandardTopAppBar(
                title = "Medicine Reminder",
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToAddEdit) {
                Icon(Icons.Default.Add, contentDescription = "Add Medicine")
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (medState.isLoading || logState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(androidx.compose.ui.Alignment.Center))
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp)
                ) {
                    item {
                        Text(
                            text = "Today's Reminders", 
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    if (todayReminders.isEmpty()) {
                        item {
                            Text(
                                text = "No reminders for today.", 
                                modifier = Modifier.padding(bottom = 16.dp),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    } else {
                        items(todayReminders) { reminder ->
                            TodayReminderCard(reminder)
                        }
                        item { Spacer(modifier = Modifier.height(16.dp)) }
                    }

                    item {
                        Text(
                            text = "All Medicines", 
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    if (medState.medicines.isEmpty()) {
                        item {
                            EmptyStateView(message = "No medicines added yet.")
                        }
                    } else {
                        items(medState.medicines) { medicine ->
                            MedicineCard(
                                medicine = medicine,
                                onClick = { onNavigateToDetail(medicine.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TodayReminderCard(reminder: TodayReminder) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Column {
                Text(text = reminder.name, style = MaterialTheme.typography.titleMedium)
                Text(text = "Dosage: ${reminder.dosage}", style = MaterialTheme.typography.bodyMedium)
            }
            Column(horizontalAlignment = androidx.compose.ui.Alignment.End) {
                Text(text = reminder.time, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                val statusText = reminder.status?.name ?: "UPCOMING"
                val statusColor = when (reminder.status) {
                    LogStatus.TAKEN -> androidx.compose.ui.graphics.Color(0xFF4CAF50)
                    LogStatus.SKIPPED -> androidx.compose.ui.graphics.Color(0xFFF44336)
                    LogStatus.PENDING -> androidx.compose.ui.graphics.Color(0xFFFF9800)
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
                Text(text = statusText, style = MaterialTheme.typography.bodySmall, color = statusColor)
            }
        }
    }
}
