package com.example.medicinereminderapp.ui.screen.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.medicinereminderapp.data.model.MedicineType
import com.example.medicinereminderapp.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    viewModel: DetailViewModel,
    medicineId: Long,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (Long) -> Unit
) {
    val medicineState by viewModel.medicine.collectAsState()
    val stats by viewModel.adherenceStats.collectAsState()
    val scrollState = rememberScrollState()

    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(medicineId) {
        viewModel.setMedicineId(medicineId)
    }

    val medicine = medicineState

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Medication Details", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    medicine?.let {
                        IconButton(onClick = { onNavigateToEdit(it.id) }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Medication")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { innerPadding ->
        if (medicine == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(scrollState)
            ) {
                // Header Visual Card
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(4.dp, RoundedCornerShape(24.dp))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f)
                                    )
                                )
                            )
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Icon based on type
                        val icon = when (medicine.type) {
                            MedicineType.TABLET -> Icons.Default.Home
                            MedicineType.CAPSULE -> Icons.Default.Info
                            MedicineType.SYRUP -> Icons.Default.PlayArrow
                            MedicineType.INJECTION -> Icons.Default.Build
                            MedicineType.INHALER -> Icons.Default.Refresh
                            else -> Icons.Default.Star
                        }
                        
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = medicine.type.name,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = medicine.name,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = medicine.dosage,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        if (medicine.instructions.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = medicine.instructions,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Adherence Statistics Card
                Text("Adherence Analytics", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Spacer(modifier = Modifier.height(8.dp))
                
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(2.dp, RoundedCornerShape(16.dp))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val (taken, total) = stats
                        val compliance = if (total == 0) 100f else (taken.toFloat() / total * 100)

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Dose Compliance",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "$taken taken out of $total tracked",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                        ) {
                            Text(
                                text = "${compliance.toInt()}%",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Active status toggle
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(2.dp, RoundedCornerShape(16.dp))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Active Alarms", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text("Enable or disable alarm triggers", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                        }
                        Switch(
                            checked = medicine.isActive,
                            onCheckedChange = { viewModel.toggleMedicineActive(medicine) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Course Schedule & Details List
                Text("Schedule Guidelines", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Spacer(modifier = Modifier.height(8.dp))

                val startStr = SimpleDateFormat("dd MMM, yyyy", Locale.getDefault()).format(Date(medicine.startDate))
                val endStr = medicine.endDate?.let {
                    SimpleDateFormat("dd MMM, yyyy", Locale.getDefault()).format(Date(it))
                } ?: "Continuous course"

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(2.dp, RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        DetailItemRow(label = "Frequency", value = medicine.frequency.lowercase().capitalize())
                        if (medicine.frequencyPattern != null) {
                            DetailItemRow(label = "Pattern Detail", value = medicine.frequencyPattern)
                        }
                        DetailItemRow(label = "Start Date", value = startStr)
                        DetailItemRow(label = "End Date", value = endStr)
                        DetailItemRow(
                            label = "Daily Times",
                            value = medicine.reminderTimes.joinToString(", ")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))

                // Delete Button
                Button(
                    onClick = { showDeleteDialog = true },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ColorSkipped),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .shadow(4.dp, RoundedCornerShape(14.dp))
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Delete Medication", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }

    // Confirmation delete alert
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Medication?", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete ${medicine?.name}? This action will cancel all its alarms and erase its history logs.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        medicine?.let {
                            viewModel.deleteMedicine(it)
                            onNavigateBack()
                        }
                    }
                ) {
                    Text("Delete", color = ColorSkipped, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun DetailItemRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
    }
}
