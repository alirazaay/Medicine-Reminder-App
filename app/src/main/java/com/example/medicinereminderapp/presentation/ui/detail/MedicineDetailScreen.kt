package com.example.medicinereminderapp.presentation.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.medicinereminderapp.data.local.entity.ReminderLogEntity
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

private data class DayStatus(
    val label: String,
    val status: LogStatus?
)

@Composable
fun MedicineDetailScreen(
    viewModel: MedicineViewModel,
    reminderLogViewModel: ReminderLogViewModel,
    medicineId: Long,
    onNavigateBack: () -> Unit,
    onEditClick: (Long) -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val logState by reminderLogViewModel.uiState.collectAsState()

    LaunchedEffect(medicineId) {
        viewModel.loadMedicineById(medicineId)
        reminderLogViewModel.loadAllLogs()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "MedTrack",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { onEditClick(medicineId) }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (state.isLoading || logState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                return@Box
            }

            val medicine = state.selectedMedicine
            if (medicine == null) {
                Text(
                    text = "Medicine not found.",
                    modifier = Modifier.align(Alignment.Center)
                )
                return@Box
            }

            val logsForMedicine = logState.logs.filter { it.medicineId == medicine.id }
            val lastSevenDays = buildLastSevenDays(logsForMedicine)
            val adherence = calculateAdherence(logsForMedicine)

            val nextDoseTime = getNextDoseTime(medicine.reminderTimes)
            val frequencyLabel = when (medicine.frequency) {
                "DAILY" -> "Daily"
                "SPECIFIC_DAYS" -> "Custom"
                "INTERVAL_DAYS" -> "Interval"
                else -> medicine.frequency
            }

            val todaySchedule = buildTodaySchedule(logsForMedicine, medicine.reminderTimes)

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(2.dp, RoundedCornerShape(18.dp))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(LightPrimary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AddCircle,
                                    contentDescription = null,
                                    tint = LightPrimary,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = medicine.name,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${medicine.dosage} ${medicine.type.name.lowercase().replaceFirstChar { it.titlecase(Locale.getDefault()) }} • $frequencyLabel",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                }

                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(1.dp, RoundedCornerShape(16.dp))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = null,
                                    tint = LightPrimary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "NEXT DOSE",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = LightPrimary
                                    )
                                    Text(
                                        text = nextDoseTime ?: "Not scheduled",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Today",
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                            )
                        }
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatCard(
                            modifier = Modifier.weight(1f),
                            title = "Adherence",
                            value = "${adherence}%",
                            progress = adherence / 100f,
                            accent = ColorTaken
                        )
                        StatCard(
                            modifier = Modifier.weight(1f),
                            title = "Remaining",
                            value = "12 pills",
                            subtitle = "Refill soon",
                            accent = ColorSkipped
                        )
                    }
                }

                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(1.dp, RoundedCornerShape(16.dp))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Home,
                                    contentDescription = null,
                                    tint = LightPrimary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Daily Schedule",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            if (todaySchedule.isEmpty()) {
                                Text(
                                    text = "No scheduled doses today.",
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            } else {
                                todaySchedule.forEachIndexed { index, item ->
                                    ScheduleRow(
                                        time = item.first,
                                        status = item.second,
                                        showDivider = index != todaySchedule.lastIndex
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(1.dp, RoundedCornerShape(16.dp))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Schedule,
                                        contentDescription = null,
                                        tint = LightPrimary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Last 7 Days",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(
                                    text = "VIEW ALL",
                                    fontSize = 12.sp,
                                    color = LightPrimary
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            val rows = lastSevenDays.take(3)
                            if (rows.isEmpty()) {
                                Text(
                                    text = "No activity yet.",
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            } else {
                                rows.forEachIndexed { index, item ->
                                    LastDayRow(item)
                                    if (index != rows.lastIndex) {
                                        HorizontalDivider(
                                            modifier = Modifier.padding(vertical = 8.dp),
                                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(
    modifier: Modifier,
    title: String,
    value: String,
    progress: Float? = null,
    subtitle: String? = null,
    accent: Color
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = modifier.shadow(1.dp, RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title.uppercase(Locale.getDefault()),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = accent
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = value, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            if (subtitle != null) {
                Text(text = subtitle, fontSize = 12.sp, color = accent)
            }
            if (progress != null) {
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    color = accent,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(100.dp))
                )
            }
        }
    }
}

@Composable
private fun ScheduleRow(time: String, status: LogStatus?, showDivider: Boolean) {
    val label = when (status) {
        LogStatus.TAKEN -> "Taken"
        LogStatus.SKIPPED -> "Skipped"
        LogStatus.MISSED -> "Missed"
        LogStatus.PENDING -> "Pending"
        null -> "Pending"
    }
    val color = when (status) {
        LogStatus.TAKEN -> ColorTaken
        LogStatus.SKIPPED -> ColorSkipped
        LogStatus.MISSED -> ColorSkipped
        LogStatus.PENDING -> ColorPending
        null -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(text = time, fontWeight = FontWeight.SemiBold)
            Text(text = label, fontSize = 12.sp, color = color)
        }
    }
    if (showDivider) {
        HorizontalDivider(
            modifier = Modifier.padding(vertical = 8.dp),
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        )
    }
}

@Composable
private fun LastDayRow(item: DayStatus) {
    val (icon, tint) = when (item.status) {
        LogStatus.TAKEN -> Icons.Default.Check to ColorTaken
        LogStatus.SKIPPED, LogStatus.MISSED -> Icons.Default.Close to ColorSkipped
        else -> Icons.Default.Schedule to ColorPending
    }
    val subtitle = when (item.status) {
        LogStatus.TAKEN -> "Taken"
        LogStatus.SKIPPED -> "Skipped"
        LogStatus.MISSED -> "Missed"
        LogStatus.PENDING, null -> "No record"
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(tint.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = tint)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(text = item.label, fontWeight = FontWeight.SemiBold)
            Text(text = subtitle, fontSize = 12.sp, color = tint)
        }
    }
}

private fun buildLastSevenDays(logs: List<ReminderLogEntity>): List<DayStatus> {
    val calendar = Calendar.getInstance()
    val result = mutableListOf<DayStatus>()

    for (offset in 1..7) {
        val dayCal = calendar.clone() as Calendar
        dayCal.add(Calendar.DAY_OF_YEAR, -offset)
        val start = dayCal.apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val end = (dayCal.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis

        val dayLogs = logs.filter { it.scheduledDateTime in start..end }
        val status = when {
            dayLogs.any { it.status == LogStatus.TAKEN } -> LogStatus.TAKEN
            dayLogs.any { it.status == LogStatus.MISSED } -> LogStatus.MISSED
            dayLogs.any { it.status == LogStatus.SKIPPED } -> LogStatus.SKIPPED
            dayLogs.any { it.status == LogStatus.PENDING } -> LogStatus.PENDING
            else -> null
        }

        val label = if (offset == 1) {
            "Yesterday"
        } else {
            SimpleDateFormat("EEEE", Locale.getDefault()).format(Date(start))
        }

        result.add(DayStatus(label = label, status = status))
    }

    return result
}

private fun calculateAdherence(logs: List<ReminderLogEntity>): Int {
    if (logs.isEmpty()) return 0
    val relevant = logs.filter { it.status != LogStatus.PENDING }
    if (relevant.isEmpty()) return 0
    val taken = relevant.count { it.status == LogStatus.TAKEN }
    return (taken.toFloat() / relevant.size * 100).toInt()
}

private fun getNextDoseTime(times: List<String>): String? {
    if (times.isEmpty()) return null
    val now = Calendar.getInstance()
    val nowMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
    val sorted = times.mapNotNull { time ->
        parseTimeToMinutes(time)?.let { it to time }
    }.sortedBy { it.first }

    val upcoming = sorted.firstOrNull { it.first >= nowMinutes }?.second ?: sorted.firstOrNull()?.second
    return upcoming?.let { formatTimeToAmPm(it) }
}

private fun parseTimeToMinutes(time: String): Int? {
    val parts = time.split(":")
    if (parts.size != 2) return null
    val hour = parts[0].toIntOrNull() ?: return null
    val minute = parts[1].toIntOrNull() ?: return null
    return hour * 60 + minute
}

private fun formatTimeToAmPm(time: String): String {
    return try {
        val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
        val output = SimpleDateFormat("h:mm a", Locale.getDefault())
        output.format(formatter.parse(time) ?: Date())
    } catch (e: Exception) {
        time
    }
}

private fun buildTodaySchedule(
    logs: List<ReminderLogEntity>,
    times: List<String>
): List<Pair<String, LogStatus?>> {
    if (times.isEmpty()) return emptyList()
    val calendar = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val start = calendar.timeInMillis
    calendar.set(Calendar.HOUR_OF_DAY, 23)
    calendar.set(Calendar.MINUTE, 59)
    calendar.set(Calendar.SECOND, 59)
    calendar.set(Calendar.MILLISECOND, 999)
    val end = calendar.timeInMillis

    val todayLogs = logs.filter { it.scheduledDateTime in start..end }
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    return times.sorted().map { time ->
        val match = todayLogs.firstOrNull {
            timeFormat.format(Date(it.scheduledDateTime)) == time
        }
        formatTimeToAmPm(time) to match?.status
    }
}