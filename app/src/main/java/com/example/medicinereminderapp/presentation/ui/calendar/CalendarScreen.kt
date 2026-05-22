package com.example.medicinereminderapp.presentation.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.medicinereminderapp.presentation.event.UiAction
import com.example.medicinereminderapp.presentation.viewmodel.ReminderLogViewModel
import com.example.medicinereminderapp.ui.theme.ColorPending
import com.example.medicinereminderapp.ui.theme.ColorSkipped
import com.example.medicinereminderapp.ui.theme.ColorTaken
import com.example.medicinereminderapp.ui.theme.LightPrimary
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun CalendarScreen(
    reminderLogViewModel: ReminderLogViewModel,
    onNavigateToHome: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val logState by reminderLogViewModel.uiState.collectAsState()

    var monthCursor by remember {
        mutableStateOf(Calendar.getInstance().apply { set(Calendar.DAY_OF_MONTH, 1) })
    }
    var selectedDay by remember { mutableStateOf(Calendar.getInstance().get(Calendar.DAY_OF_MONTH)) }
    var viewMode by remember { mutableStateOf("MONTH") }

    LaunchedEffect(Unit) {
        reminderLogViewModel.loadAllLogs()
    }

    val monthLabel = remember(monthCursor) {
        SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(monthCursor.time)
    }

    val daysInMonth = remember(monthCursor) { monthCursor.getActualMaximum(Calendar.DAY_OF_MONTH) }
    val firstDayOfWeek = remember(monthCursor) { monthCursor.get(Calendar.DAY_OF_WEEK) }
    val leadingBlanks = (firstDayOfWeek - Calendar.SUNDAY).coerceAtLeast(0)
    val days = remember(monthCursor) {
        val blanks = List(leadingBlanks) { null }
        val nums = (1..daysInMonth).map { it }
        blanks + nums
    }

    val selectedDateLogs = remember(logState.logs, monthCursor, selectedDay) {
        logsForDate(logState.logs, monthCursor, selectedDay)
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
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
                                imageVector = Icons.Default.Home,
                                contentDescription = null,
                                tint = LightPrimary
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "MedTrack",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif
                        )
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Notifications, contentDescription = "Notifications")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = monthLabel,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Row {
                        IconButton(onClick = {
                            monthCursor = (monthCursor.clone() as Calendar).apply {
                                add(Calendar.MONTH, -1)
                                set(Calendar.DAY_OF_MONTH, 1)
                            }
                            selectedDay = 1
                        }) {
                            Icon(Icons.Default.ChevronLeft, contentDescription = "Previous")
                        }
                        IconButton(onClick = {
                            monthCursor = (monthCursor.clone() as Calendar).apply {
                                add(Calendar.MONTH, 1)
                                set(Calendar.DAY_OF_MONTH, 1)
                            }
                            selectedDay = 1
                        }) {
                            Icon(Icons.Default.ChevronRight, contentDescription = "Next")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(4.dp)
                ) {
                    ModeChip(
                        label = "Month",
                        selected = viewMode == "MONTH",
                        onClick = { viewMode = "MONTH" }
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    ModeChip(
                        label = "Week",
                        selected = viewMode == "WEEK",
                        onClick = { viewMode = "WEEK" }
                    )
                }
            }
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToHome,
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home") }
                )
                NavigationBarItem(
                    selected = true,
                    onClick = { },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(2.dp, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        listOf("S", "M", "T", "W", "T", "F", "S").forEach { label ->
                            Text(
                                text = label,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                modifier = Modifier.weight(1f),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    days.chunked(7).forEach { week ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            week.forEach { day ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (day != null) {
                                        val isSelected = day == selectedDay
                                        val dayColor = if (isSelected) LightPrimary else Color.Transparent
                                        val dayTextColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                        val statusColor = dayStatusColor(logState.logs, monthCursor, day)

                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(dayColor)
                                                .clickable {
                                                    selectedDay = day
                                                }
                                        ) {
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = day.toString(),
                                                fontSize = 13.sp,
                                                color = dayTextColor,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            if (statusColor != null) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(5.dp)
                                                        .clip(CircleShape)
                                                        .background(statusColor)
                                                )
                                            } else {
                                                Spacer(modifier = Modifier.height(5.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                LegendDot(label = "All Taken", color = ColorTaken)
                LegendDot(label = "Partial", color = ColorPending)
                LegendDot(label = "Missed", color = ColorSkipped)
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "${monthLabel.split(" ").first()} ${selectedDay}th Details",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (selectedDateLogs.isEmpty()) {
                Text(
                    text = "No logs for this date.",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            } else {
                selectedDateLogs.sortedBy { it.scheduledDateTime }.forEach { log ->
                    CalendarLogCard(
                        log = log,
                        onMarkTaken = {
                            reminderLogViewModel.onAction(
                                UiAction.UpdateLogStatus(
                                    logId = log.id,
                                    status = LogStatus.TAKEN,
                                    actionTime = System.currentTimeMillis()
                                )
                            )
                        }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
private fun RowScope.ModeChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) MaterialTheme.colorScheme.surface else Color.Transparent)
            .border(
                1.dp,
                if (selected) LightPrimary else Color.Transparent,
                RoundedCornerShape(10.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontWeight = FontWeight.SemiBold,
            color = if (selected) LightPrimary else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun LegendDot(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = label, fontSize = 12.sp)
    }
}

@Composable
private fun CalendarLogCard(log: ReminderLogEntity, onMarkTaken: () -> Unit) {
    val time = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(log.scheduledDateTime))
    val statusText = when (log.status) {
        LogStatus.TAKEN -> "TAKEN"
        LogStatus.SKIPPED -> "SKIPPED"
        LogStatus.MISSED -> "MISSED"
        LogStatus.PENDING -> "PENDING"
    }
    val statusColor = when (log.status) {
        LogStatus.TAKEN -> ColorTaken
        LogStatus.SKIPPED -> ColorSkipped
        LogStatus.MISSED -> ColorSkipped
        LogStatus.PENDING -> ColorPending
    }

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
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(LightPrimary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = LightPrimary)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(log.medicineName, fontWeight = FontWeight.Bold)
                    Text(
                        text = "${log.dosage} • $time",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            if (log.status == LogStatus.PENDING) {
                Button(
                    onClick = onMarkTaken,
                    colors = ButtonDefaults.buttonColors(containerColor = LightPrimary),
                    shape = RoundedCornerShape(999.dp)
                ) {
                    Text("Mark Taken", color = Color.White)
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (log.status == LogStatus.TAKEN) Icons.Default.Check else Icons.Default.Close,
                        contentDescription = null,
                        tint = statusColor
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = statusText,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                }
            }
        }
    }
}

private fun logsForDate(
    logs: List<ReminderLogEntity>,
    monthCursor: Calendar,
    day: Int
): List<ReminderLogEntity> {
    val cal = (monthCursor.clone() as Calendar).apply {
        set(Calendar.DAY_OF_MONTH, day)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val start = cal.timeInMillis
    val end = (cal.clone() as Calendar).apply {
        set(Calendar.HOUR_OF_DAY, 23)
        set(Calendar.MINUTE, 59)
        set(Calendar.SECOND, 59)
        set(Calendar.MILLISECOND, 999)
    }.timeInMillis

    return logs.filter { it.scheduledDateTime in start..end }
}

private fun dayStatusColor(
    logs: List<ReminderLogEntity>,
    monthCursor: Calendar,
    day: Int
): Color? {
    val dayLogs = logsForDate(logs, monthCursor, day)
    if (dayLogs.isEmpty()) return null

    return when {
        dayLogs.any { it.status == LogStatus.MISSED } -> ColorSkipped
        dayLogs.any { it.status == LogStatus.SKIPPED } -> ColorPending
        dayLogs.any { it.status == LogStatus.PENDING } -> ColorPending
        dayLogs.all { it.status == LogStatus.TAKEN } -> ColorTaken
        else -> ColorPending
    }
}