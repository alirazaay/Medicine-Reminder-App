package com.example.medicinereminderapp.presentation.ui.add_edit

import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.medicinereminderapp.data.local.entity.MedicineEntity
import com.example.medicinereminderapp.domain.model.MedicineType
import com.example.medicinereminderapp.presentation.event.UiAction
import com.example.medicinereminderapp.presentation.event.UiEvent
import com.example.medicinereminderapp.presentation.viewmodel.MedicineViewModel
import com.example.medicinereminderapp.ui.theme.LightPrimary
import kotlinx.coroutines.flow.collectLatest
import androidx.compose.runtime.collectAsState
import java.util.Locale

private data class FormOption(
    val label: String,
    val type: MedicineType,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditMedicineScreen(
    viewModel: MedicineViewModel,
    medicineId: Long,
    onNavigateBack: () -> Unit,
    snackbarHostState: SnackbarHostState
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var name by remember { mutableStateOf("") }
    var dosageValue by remember { mutableStateOf("") }
    var dosageUnit by remember { mutableStateOf("mg") }
    var type by remember { mutableStateOf(MedicineType.CAPSULE) }
    var instructions by remember { mutableStateOf("") }
    var frequency by remember { mutableStateOf("DAILY") }
    val selectedDays = remember { mutableStateListOf<String>() }
    var firstDoseTime by remember { mutableStateOf("08:00") }
    var startDate by remember { mutableStateOf(System.currentTimeMillis()) }
    var endDate by remember { mutableStateOf<Long?>(null) }

    var unitExpanded by remember { mutableStateOf(false) }
    val units = listOf("mg", "ml", "g", "IU", "drops")

    val formOptions = listOf(
        FormOption("Capsule", MedicineType.CAPSULE, Icons.Default.Info),
        FormOption("Tablet", MedicineType.TABLET, Icons.Default.Home),
        FormOption("Liquid", MedicineType.SYRUP, Icons.Default.PlayArrow),
        FormOption("Injection", MedicineType.INJECTION, Icons.Default.Build)
    )

    LaunchedEffect(medicineId) {
        if (medicineId != -1L) {
            viewModel.loadMedicineById(medicineId)
        }
    }

    LaunchedEffect(state.selectedMedicine) {
        if (medicineId != -1L && state.selectedMedicine != null) {
            val med = state.selectedMedicine!!
            name = med.name
            val parsed = parseDosage(med.dosage)
            dosageValue = parsed.first
            dosageUnit = parsed.second
            type = med.type
            instructions = med.instructions
            frequency = med.frequency
            selectedDays.clear()
            if (med.frequency == "SPECIFIC_DAYS") {
                med.frequencyPattern?.split(",")?.forEach { day ->
                    selectedDays.add(day.trim().lowercase())
                }
            }
            firstDoseTime = med.reminderTimes.firstOrNull() ?: "08:00"
            startDate = med.startDate
            endDate = med.endDate
        }
    }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is UiEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
                is UiEvent.NavigateBack -> onNavigateBack()
                else -> Unit
            }
        }
    }

    fun pickTime() {
        val parts = firstDoseTime.split(":")
        val hour = parts.getOrNull(0)?.toIntOrNull() ?: 8
        val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0
        TimePickerDialog(
            context,
            { _, pickedHour, pickedMinute ->
                firstDoseTime = String.format(Locale.getDefault(), "%02d:%02d", pickedHour, pickedMinute)
            },
            hour,
            minute,
            false
        ).show()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
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
                            fontFamily = FontFamily.Serif,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "STEP 1 OF 4",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = LightPrimary
                    )
                    Text(
                        text = "Basic Info",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { 0.25f },
                    color = LightPrimary,
                    trackColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(100.dp))
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = if (medicineId == -1L) "Add Medicine" else "Edit Medicine",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text("Medicine Name", fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = { Text("e.g. Amoxicillin") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text("Form", fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(formOptions) { option ->
                        FilterChip(
                            selected = type == option.type,
                            onClick = { type = option.type },
                            label = { Text(option.label) },
                            leadingIcon = {
                                Icon(option.icon, contentDescription = null)
                            },
                            shape = RoundedCornerShape(999.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = LightPrimary,
                                selectedLabelColor = Color.White,
                                selectedLeadingIconColor = Color.White
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Dosage", fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = dosageValue,
                            onValueChange = { dosageValue = it },
                            placeholder = { Text("250") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text("Unit", fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(6.dp))
                        ExposedDropdownMenuBox(
                            expanded = unitExpanded,
                            onExpandedChange = { unitExpanded = !unitExpanded }
                        ) {
                            OutlinedTextField(
                                value = dosageUnit,
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = unitExpanded)
                                },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = unitExpanded,
                                onDismissRequest = { unitExpanded = false }
                            ) {
                                units.forEach { unit ->
                                    TextButton(
                                        onClick = {
                                            dosageUnit = unit
                                            unitExpanded = false
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(unit)
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(2.dp, RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Frequency", fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp),
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(4.dp)
                        ) {
                            FrequencySegment(
                                label = "Every Day",
                                selected = frequency == "DAILY",
                                onClick = { frequency = "DAILY" }
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            FrequencySegment(
                                label = "Custom Days",
                                selected = frequency == "SPECIFIC_DAYS",
                                onClick = { frequency = "SPECIFIC_DAYS" }
                            )
                        }

                        if (frequency == "SPECIFIC_DAYS") {
                            Spacer(modifier = Modifier.height(10.dp))
                            DaysRow(selectedDays = selectedDays)
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        Text("First Dose Time", fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedCard(
                            onClick = { pickTime() },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
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
                                    Text(
                                        text = firstDoseTime,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.WbSunny,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedCard(
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddAPhoto,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Add Photo of Pill/Prescription",
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Helps you identify it later",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("Notes", fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = instructions,
                    onValueChange = { instructions = it },
                    placeholder = { Text("With food, after breakfast") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        val dosageText = buildString {
                            if (dosageValue.isNotBlank()) {
                                append(dosageValue.trim())
                                if (dosageUnit.isNotBlank()) {
                                    append(" ")
                                    append(dosageUnit.trim())
                                }
                            }
                        }

                        val pattern = if (frequency == "SPECIFIC_DAYS") {
                            selectedDays.joinToString(",")
                        } else {
                            null
                        }

                        val med = MedicineEntity(
                            id = if (medicineId == -1L) 0 else medicineId,
                            name = name.trim(),
                            dosage = dosageText,
                            type = type,
                            instructions = instructions.trim(),
                            startDate = startDate,
                            endDate = endDate,
                            frequency = if (frequency == "SPECIFIC_DAYS") "SPECIFIC_DAYS" else "DAILY",
                            frequencyPattern = pattern,
                            reminderTimes = listOf(firstDoseTime),
                            isActive = true
                        )

                        if (medicineId == -1L) {
                            viewModel.onAction(UiAction.AddMedicine(med))
                        } else {
                            viewModel.onAction(UiAction.UpdateMedicine(med))
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = LightPrimary)
                ) {
                    Text("Continue", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun RowScope.FrequencySegment(label: String, selected: Boolean, onClick: () -> Unit) {
    val background = if (selected) LightPrimary.copy(alpha = 0.12f) else Color.Transparent
    val border = if (selected) LightPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
    val textColor = if (selected) LightPrimary else MaterialTheme.colorScheme.onSurface

    Box(
        modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(10.dp))
            .background(background)
            .border(1.dp, border, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = label, color = textColor, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
    }
}

@Composable
private fun DaysRow(selectedDays: MutableList<String>) {
    val days = listOf("S", "M", "T", "W", "T", "F", "S")
    val dayKeys = listOf("sunday", "monday", "tuesday", "wednesday", "thursday", "friday", "saturday")

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        days.forEachIndexed { index, label ->
            val dayKey = dayKeys[index]
            val selected = selectedDays.contains(dayKey)
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(if (selected) LightPrimary else Color.Transparent)
                    .border(
                        1.dp,
                        if (selected) LightPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                        CircleShape
                    )
                    .clickable {
                        if (selected) selectedDays.remove(dayKey) else selectedDays.add(dayKey)
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

private fun parseDosage(dosage: String): Pair<String, String> {
    val regex = Regex("""^\s*([0-9]+(?:\.[0-9]+)?)\s*([a-zA-Z]+)?\s*$""")
    val match = regex.find(dosage)
    if (match != null) {
        val value = match.groupValues.getOrNull(1).orEmpty()
        val unit = match.groupValues.getOrNull(2).orEmpty().ifBlank { "mg" }
        return value to unit
    }
    return dosage.trim() to "mg"
}