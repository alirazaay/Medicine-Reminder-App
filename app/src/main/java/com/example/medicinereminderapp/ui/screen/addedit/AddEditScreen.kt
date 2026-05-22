package com.example.medicinereminderapp.ui.screen.addedit

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.medicinereminderapp.domain.model.MedicineType
import com.example.medicinereminderapp.ui.theme.LightPrimary
import com.example.medicinereminderapp.util.DateTimeUtils
import kotlinx.coroutines.flow.collectLatest
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditScreen(
    viewModel: AddEditViewModel,
    medicineId: Long = -1L,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    LaunchedEffect(medicineId) {
        viewModel.loadMedicine(medicineId)
    }

    LaunchedEffect(Unit) {
        viewModel.isSaved.collectLatest { saved ->
            if (saved) {
                onNavigateBack()
            }
        }
    }

    fun pickTime() {
        val calendar = Calendar.getInstance()
        TimePickerDialog(
            context,
            { _, hour, minute ->
                val formatted = String.format(Locale.getDefault(), "%02d:%02d", hour, minute)
                viewModel.addTime(formatted)
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            false
        ).show()
    }

    fun pickDate(isStartDate: Boolean) {
        val calendar = Calendar.getInstance()
        if (!isStartDate && viewModel.endDate != null) {
            calendar.timeInMillis = viewModel.endDate!!
        } else if (isStartDate) {
            calendar.timeInMillis = viewModel.startDate
        }
        DatePickerDialog(
            context,
            { _, year, month, day ->
                val selected = Calendar.getInstance().apply {
                    set(year, month, day, 0, 0, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                if (isStartDate) {
                    viewModel.startDate = selected.timeInMillis
                } else {
                    viewModel.endDate = selected.timeInMillis
                }
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (medicineId == -1L) "Add Medication" else "Edit Medication",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(scrollState)
        ) {
            // Medicine Name Input
            OutlinedTextField(
                value = viewModel.name,
                onValueChange = { viewModel.name = it },
                label = { Text("Medication Name") },
                placeholder = { Text("e.g. Paracetamol") },
                leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                shape = RoundedCornerShape(12.dp)
            )

            // Dosage input
            OutlinedTextField(
                value = viewModel.dosage,
                onValueChange = { viewModel.dosage = it },
                label = { Text("Dosage") },
                placeholder = { Text("e.g. 1 tablet, 5 ml") },
                leadingIcon = { Icon(Icons.Default.Info, contentDescription = null) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                shape = RoundedCornerShape(12.dp)
            )

            // Instructions input
            OutlinedTextField(
                value = viewModel.instructions,
                onValueChange = { viewModel.instructions = it },
                label = { Text("Instructions (Optional)") },
                placeholder = { Text("e.g. After food, take with water") },
                leadingIcon = { Icon(Icons.Default.Star, contentDescription = null) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Medicine Type Selection Grid
            Text("Medicine Form", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))
            
            // Standard medical type options grid
            val medicineTypes = MedicineType.values()
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(medicineTypes) { type ->
                    val isSelected = viewModel.type == type
                    val icon = when (type) {
                        MedicineType.TABLET -> Icons.Default.Home
                        MedicineType.CAPSULE -> Icons.Default.Info
                        MedicineType.SYRUP -> Icons.Default.PlayArrow
                        MedicineType.INJECTION -> Icons.Default.Build
                        MedicineType.INHALER -> Icons.Default.Refresh
                        MedicineType.DROPS -> Icons.Default.Search
                        MedicineType.CREAM -> Icons.Default.Check
                        else -> Icons.Default.Star
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                else MaterialTheme.colorScheme.surface
                            )
                            .border(
                                1.dp,
                                if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                                RoundedCornerShape(12.dp)
                            )
                            .clickable { viewModel.type = type }
                            .padding(vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = type.name,
                            tint = if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = type.name.lowercase().capitalize(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Frequency Tab Selector
            Text("Reminder Frequency", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))

            val frequencies = listOf("DAILY", "SPECIFIC_DAYS", "INTERVAL_DAYS")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f))
                    .padding(4.dp)
            ) {
                frequencies.forEach { freq ->
                    val isSelected = viewModel.frequency == freq
                    val label = when (freq) {
                        "DAILY" -> "Daily"
                        "SPECIFIC_DAYS" -> "Days"
                        "INTERVAL_DAYS" -> "Interval"
                        else -> freq
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent)
                            .clickable { viewModel.frequency = freq }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Extra frequency UI inputs depending on selected freq tab
            when (viewModel.frequency) {
                "SPECIFIC_DAYS" -> {
                    val daysList = listOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")
                    Text("Select Days", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        daysList.forEach { dayName ->
                            val lower = dayName.lowercase()
                            val letter = dayName.take(1)
                            val isSelected = viewModel.selectedDays.contains(lower)

                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.surface
                                    )
                                    .border(
                                        1.dp,
                                        if (isSelected) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                                        CircleShape
                                    )
                                    .clickable { viewModel.toggleDay(lower) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = letter,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                    else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
                "INTERVAL_DAYS" -> {
                    OutlinedTextField(
                        value = viewModel.intervalDays,
                        onValueChange = { viewModel.intervalDays = it },
                        label = { Text("Every X Days") },
                        placeholder = { Text("e.g. 2 for every other day") },
                        leadingIcon = { Icon(Icons.Default.Refresh, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Start & End Date Selectors
            Text("Medication Course Duration", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Start Date display
                val startStr = SimpleDateFormat("dd MMM, yyyy", Locale.getDefault()).format(Date(viewModel.startDate))
                OutlinedCard(
                    onClick = { pickDate(true) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Start Date", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(startStr, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // End Date display
                val endStr = if (viewModel.hasEndDate && viewModel.endDate != null) {
                    SimpleDateFormat("dd MMM, yyyy", Locale.getDefault()).format(Date(viewModel.endDate!!))
                } else "Continuous Course"

                OutlinedCard(
                    onClick = {
                        if (!viewModel.hasEndDate) {
                            viewModel.hasEndDate = true
                            if (viewModel.endDate == null) {
                                val cal = Calendar.getInstance().apply { add(Calendar.MONTH, 1) }
                                viewModel.endDate = cal.timeInMillis
                            }
                        }
                        pickDate(false)
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("End Date", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            Switch(
                                checked = viewModel.hasEndDate,
                                onCheckedChange = { checked ->
                                    viewModel.hasEndDate = checked
                                    if (checked && viewModel.endDate == null) {
                                        val cal = Calendar.getInstance().apply { add(Calendar.MONTH, 1) }
                                        viewModel.endDate = cal.timeInMillis
                                    }
                                },
                                modifier = Modifier
                                    .scale(0.8f)
                                    .height(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(endStr, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Reminder Times Compiler
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Reminder Times", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                IconButton(
                    onClick = { pickTime() },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    ),
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Time", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            if (viewModel.reminderTimes.isEmpty()) {
                Text(
                    text = "Please add at least one reminder time.",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            } else {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(viewModel.reminderTimes) { timeStr ->
                        InputChip(
                            selected = true,
                            onClick = { },
                            label = { Text(timeStr, fontWeight = FontWeight.Bold) },
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Remove time",
                                    modifier = Modifier
                                        .size(14.dp)
                                        .clickable { viewModel.removeTime(timeStr) }
                                )
                            },
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Save medication button
            Button(
                onClick = { viewModel.saveMedicine() },
                enabled = viewModel.name.isNotBlank() && viewModel.dosage.isNotBlank() && viewModel.reminderTimes.isNotEmpty(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .shadow(4.dp, RoundedCornerShape(14.dp)),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = LightPrimary,
                    disabledContainerColor = LightPrimary.copy(alpha = 0.3f)
                )
            ) {
                Text("Save Medication", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
