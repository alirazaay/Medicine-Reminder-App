package com.example.medicinereminderapp.presentation.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Brightness5
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.medicinereminderapp.domain.model.LogStatus
import com.example.medicinereminderapp.presentation.viewmodel.MedicineViewModel
import com.example.medicinereminderapp.presentation.viewmodel.ReminderLogViewModel
import com.example.medicinereminderapp.ui.theme.ColorPending
import com.example.medicinereminderapp.ui.theme.ColorSkipped
import com.example.medicinereminderapp.ui.theme.ColorTaken
import com.example.medicinereminderapp.ui.theme.LightPrimary
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class TodayReminder(
    val medicineId: Long,
    val name: String,
    val dosage: String,
    val time: String,
    val status: LogStatus?,
    val instructions: String
)

@Composable
fun HomeScreen(
    medicineViewModel: MedicineViewModel,
    reminderLogViewModel: ReminderLogViewModel,
    onNavigateToAddEdit: () -> Unit,
    onNavigateToDetail: (Long) -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToHistory: () -> Unit = {},
    onNavigateToCalendar: () -> Unit = {}
) {
    val medState by medicineViewModel.uiState.collectAsState()
    val logState by reminderLogViewModel.uiState.collectAsState()

    val todayReminders = remember(medState.medicines, logState.logs) {
        val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val reminders = mutableListOf<TodayReminder>()

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startOfDay = calendar.timeInMillis
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val endOfDay = calendar.timeInMillis

        val todayLogs = logState.logs.filter { it.scheduledDateTime in startOfDay..endOfDay }

        medState.medicines.filter { it.isActive }.forEach { medicine ->
            medicine.reminderTimes.forEach { timeStr ->
                val logForTime = todayLogs.find { log ->
                    log.medicineId == medicine.id && dateFormat.format(Date(log.scheduledDateTime)) == timeStr
                }
                reminders.add(
                    TodayReminder(
                        medicineId = medicine.id,
                        name = medicine.name,
                        dosage = medicine.dosage,
                        time = timeStr,
                        status = logForTime?.status,
                        instructions = medicine.instructions
                    )
                )
            }
        }
        reminders.sortBy { it.time }
        reminders
    }

    val nowMinutes = remember {
        val now = Calendar.getInstance()
        now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
    }

    val remindersWithMinutes = todayReminders.mapNotNull { reminder ->
        parseTimeToMinutes(reminder.time)?.let { minutes -> minutes to reminder }
    }.sortedBy { it.first }

    val upcomingReminder = remindersWithMinutes.firstOrNull { it.first >= nowMinutes }?.second
        ?: remindersWithMinutes.firstOrNull()?.second

    val adherenceProgress = if (todayReminders.isEmpty()) 0f else {
        val taken = todayReminders.count { it.status == LogStatus.TAKEN }
        taken.toFloat() / todayReminders.size
    }

    val dateLabel = remember {
        SimpleDateFormat("EEEE, MMM d", Locale.getDefault()).format(Date())
    }

    val greeting = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when (hour) {
            in 5..11 -> "Good Morning"
            in 12..16 -> "Good Afternoon"
            in 17..21 -> "Good Evening"
            else -> "Hello"
        }
    }

    val displayName = "User"

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToAddEdit) {
                Icon(Icons.Default.Add, contentDescription = "Add Medicine")
            }
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = true,
                    onClick = { },
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
                    selected = false,
                    onClick = onNavigateToSettings,
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                    label = { Text("Settings") }
                )
            }
        }
    ) { padding ->
        if (medState.isLoading || logState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(LightPrimary.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalHospital,
                                contentDescription = null,
                                tint = LightPrimary
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "MedTrack",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notifications"
                        )
                    }
                }
            }

            item {
                Column {
                    Text(
                        text = "$greeting, $displayName",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = dateLabel,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            item {
                AdherenceCard(
                    progress = adherenceProgress,
                    total = todayReminders.size,
                    taken = todayReminders.count { it.status == LogStatus.TAKEN }
                )
            }

            item {
                UpNextCard(
                    reminder = upcomingReminder,
                    onTakeNow = { reminder ->
                        onNavigateToDetail(reminder.medicineId)
                    }
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Today's Schedule",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "See All",
                        fontSize = 13.sp,
                        color = LightPrimary,
                        modifier = Modifier.clickable(onClick = onNavigateToHistory)
                    )
                }
            }

            remindersSectionItems(remindersWithMinutes)
        }
    }
}

private fun parseTimeToMinutes(time: String): Int? {
    val parts = time.split(":")
    if (parts.size != 2) return null
    val hour = parts[0].toIntOrNull() ?: return null
    val minute = parts[1].toIntOrNull() ?: return null
    return hour * 60 + minute
}

@Composable
private fun AdherenceCard(progress: Float, taken: Int, total: Int) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(18.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(LightPrimary.copy(alpha = 0.08f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    progress = { progress },
                    strokeWidth = 8.dp,
                    color = LightPrimary
                )
                Text(
                    text = "${(progress * 100).toInt()}%",
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Daily Adherence", fontWeight = FontWeight.Bold)
                Text(
                    text = "You've taken $taken out of $total medications today.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = ColorTaken,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "On track",
                        fontSize = 12.sp,
                        color = ColorTaken
                    )
                }
            }
        }
    }
}

@Composable
private fun UpNextCard(
    reminder: TodayReminder?,
    onTakeNow: (TodayReminder) -> Unit
) {
    val cardColor = Color(0xFF2F6CB3)
    val accentColor = Color(0xFF1B4F8B)

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(3.dp, RoundedCornerShape(18.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "UP NEXT",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                if (reminder != null) {
                    TimePill(time = reminder.time)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = reminder?.name ?: "No upcoming medications",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = reminder?.let { "${it.dosage} • ${it.instructions.ifBlank { "Take as directed" }}" }
                    ?: "You are all caught up for now.",
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.85f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { if (reminder != null) onTakeNow(reminder) },
                    enabled = reminder != null,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = accentColor
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Take Now", fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.15f))
                        .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = "Snooze",
                        tint = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun TimePill(time: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(Color.White.copy(alpha = 0.2f))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(text = time, color = Color.White, fontSize = 12.sp)
    }
}

private data class Section(
    val title: String,
    val icon: ImageVector,
    val items: List<TodayReminder>
)

private fun remindersSectionItems(
    remindersWithMinutes: List<Pair<Int, TodayReminder>>
): LazyListScope.() -> Unit = {
    val morning = remindersWithMinutes.filter { it.first < 12 * 60 }.map { it.second }
    val noon = remindersWithMinutes.filter { it.first in (12 * 60)..(17 * 60) }.map { it.second }
    val evening = remindersWithMinutes.filter { it.first > 17 * 60 }.map { it.second }

    val sections = listOf(
        Section("MORNING", Icons.Default.WbSunny, morning),
        Section("NOON", Icons.Default.Brightness5, noon),
        Section("EVENING", Icons.Default.NightsStay, evening)
    ).filter { it.items.isNotEmpty() }

    if (sections.isEmpty()) {
        item {
            Text(
                text = "No reminders scheduled today.",
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }

    sections.forEach { section ->
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = section.icon,
                    contentDescription = null,
                    tint = LightPrimary
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = section.title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = LightPrimary
                )
            }
        }
        items(section.items) { reminder ->
            ScheduleItemCard(reminder = reminder)
        }
    }
}

@Composable
private fun ScheduleItemCard(reminder: TodayReminder) {
    val statusColor = when (reminder.status) {
        LogStatus.TAKEN -> ColorTaken
        LogStatus.SKIPPED -> ColorSkipped
        LogStatus.PENDING -> ColorPending
        null -> MaterialTheme.colorScheme.outline
        else -> MaterialTheme.colorScheme.outline
    }

    val isTaken = reminder.status == LogStatus.TAKEN

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(1.dp, RoundedCornerShape(16.dp))
            .border(1.dp, statusColor.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(LightPrimary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.LocalHospital,
                    contentDescription = null,
                    tint = LightPrimary
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(reminder.name, fontWeight = FontWeight.SemiBold)
                Text(
                    text = "${reminder.dosage} • ${reminder.time}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(if (isTaken) ColorTaken.copy(alpha = 0.15f) else Color.Transparent)
                    .border(1.dp, statusColor.copy(alpha = 0.6f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (isTaken) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Taken",
                        tint = ColorTaken
                    )
                }
            }
        }
    }
}
