package com.example.medicinereminderapp.presentation.ui.log

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.medicinereminderapp.data.local.entity.ReminderLogEntity
import com.example.medicinereminderapp.domain.model.LogStatus
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
fun ReminderLogScreen(
    viewModel: ReminderLogViewModel,
    onNavigateToHome: () -> Unit,
    onNavigateToCalendar: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("ALL") }

    LaunchedEffect(Unit) {
        viewModel.loadAllLogs()
    }

    val filteredLogs = remember(state.logs, searchQuery, selectedFilter) {
        state.logs
            .filter { log ->
                log.medicineName.contains(searchQuery.trim(), ignoreCase = true)
            }
            .filter { log ->
                when (selectedFilter) {
                    "TAKEN" -> log.status == LogStatus.TAKEN
                    "MISSED" -> log.status == LogStatus.MISSED
                    "THIS_WEEK" -> isWithinLastDays(log.scheduledDateTime, 7)
                    else -> true
                }
            }
            .sortedByDescending { it.scheduledDateTime }
    }

    val groupedLogs = remember(filteredLogs) { groupLogsByDate(filteredLogs) }

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
                        Text(
                            text = "medical_services",
                            fontSize = 18.sp,
                            color = LightPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "MedTrack",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif
                        )
                    }
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = "Export PDF")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search medication history...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FilterPill(
                        label = "All",
                        selected = selectedFilter == "ALL",
                        onClick = { selectedFilter = "ALL" }
                    )
                    FilterPill(
                        label = "Taken",
                        selected = selectedFilter == "TAKEN",
                        onClick = { selectedFilter = "TAKEN" }
                    )
                    FilterPill(
                        label = "Missed",
                        selected = selectedFilter == "MISSED",
                        onClick = { selectedFilter = "MISSED" }
                    )
                    FilterPill(
                        label = "This Week",
                        selected = selectedFilter == "THIS_WEEK",
                        onClick = { selectedFilter = "THIS_WEEK" }
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
                    selected = false,
                    onClick = onNavigateToCalendar,
                    icon = { Icon(Icons.Default.CalendarToday, contentDescription = "Calendar") },
                    label = { Text("Calendar") }
                )
                NavigationBarItem(
                    selected = true,
                    onClick = { },
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (groupedLogs.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No history logs found.",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            groupedLogs.forEach { (label, logs) ->
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = label,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
                        )
                    }
                }

                items(logs, key = { it.id }) { log ->
                    HistoryLogCard(log = log)
                }
            }
        }
    }
}

@Composable
private fun FilterPill(label: String, selected: Boolean, onClick: () -> Unit) {
    val background = if (selected) LightPrimary else Color.Transparent
    val border = if (selected) LightPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
    val textColor = if (selected) Color.White else MaterialTheme.colorScheme.onSurface

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(background)
            .border(1.dp, border, RoundedCornerShape(999.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 8.dp)
    ) {
        Text(text = label, color = textColor, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun HistoryLogCard(log: ReminderLogEntity) {
    val statusColor = when (log.status) {
        LogStatus.TAKEN -> ColorTaken
        LogStatus.SKIPPED -> ColorSkipped
        LogStatus.MISSED -> ColorSkipped
        LogStatus.PENDING -> ColorPending
    }

    val statusLabel = when (log.status) {
        LogStatus.TAKEN -> "Taken"
        LogStatus.MISSED -> "Missed"
        LogStatus.SKIPPED -> "Skipped"
        LogStatus.PENDING -> "Pending"
    }

    val scheduledTime = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(log.scheduledDateTime))
    val takenTime = log.actionDateTime?.let {
        SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(it))
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(1.dp, RoundedCornerShape(16.dp))
            .border(
                1.dp,
                statusColor.copy(alpha = 0.25f),
                RoundedCornerShape(16.dp)
            )
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
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(statusColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (log.status == LogStatus.TAKEN) Icons.Default.Check else Icons.Default.Close,
                        contentDescription = null,
                        tint = statusColor
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(text = log.medicineName, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = buildString {
                            append("Sch: ")
                            append(scheduledTime)
                            if (takenTime != null && log.status == LogStatus.TAKEN) {
                                append("   22  Taken: ")
                                append(takenTime)
                            }
                        },
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            StatusPill(label = statusLabel, color = statusColor)
        }
    }
}

@Composable
private fun StatusPill(label: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(color.copy(alpha = 0.2f))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(text = label, color = color, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
    }
}

private fun groupLogsByDate(logs: List<ReminderLogEntity>): List<Pair<String, List<ReminderLogEntity>>> {
    if (logs.isEmpty()) return emptyList()
    val formatter = SimpleDateFormat("MMM d", Locale.getDefault())

    val grouped = logs.groupBy { log ->
        val label = when {
            isToday(log.scheduledDateTime) -> "Today"
            isYesterday(log.scheduledDateTime) -> "Yesterday"
            else -> formatter.format(Date(log.scheduledDateTime))
        }
        label
    }

    return grouped.entries
        .sortedByDescending { entry ->
            entry.value.maxOfOrNull { it.scheduledDateTime } ?: 0L
        }
        .map { it.key to it.value }
}

private fun isToday(timestamp: Long): Boolean {
    val cal = Calendar.getInstance()
    val start = cal.apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
    val end = (cal.clone() as Calendar).apply {
        set(Calendar.HOUR_OF_DAY, 23)
        set(Calendar.MINUTE, 59)
        set(Calendar.SECOND, 59)
        set(Calendar.MILLISECOND, 999)
    }.timeInMillis
    return timestamp in start..end
}

private fun isYesterday(timestamp: Long): Boolean {
    val cal = Calendar.getInstance().apply {
        add(Calendar.DAY_OF_YEAR, -1)
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
    return timestamp in start..end
}

private fun isWithinLastDays(timestamp: Long, days: Int): Boolean {
    val cal = Calendar.getInstance().apply {
        add(Calendar.DAY_OF_YEAR, -days)
    }
    return timestamp >= cal.timeInMillis
}