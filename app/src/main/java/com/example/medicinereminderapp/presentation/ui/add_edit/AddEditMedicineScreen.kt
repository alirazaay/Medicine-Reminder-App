package com.example.medicinereminderapp.presentation.ui.add_edit

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.medicinereminderapp.data.local.entity.MedicineEntity
import com.example.medicinereminderapp.domain.model.MedicineType
import com.example.medicinereminderapp.presentation.event.UiAction
import com.example.medicinereminderapp.presentation.event.UiEvent
import com.example.medicinereminderapp.presentation.ui.components.StandardTopAppBar
import com.example.medicinereminderapp.presentation.viewmodel.MedicineViewModel
import kotlinx.coroutines.flow.collectLatest

@Composable
fun AddEditMedicineScreen(
    viewModel: MedicineViewModel,
    medicineId: Long,
    onNavigateBack: () -> Unit,
    snackbarHostState: SnackbarHostState
) {
    val state by viewModel.uiState.collectAsState()
    
    var name by remember { mutableStateOf("") }
    var dosage by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(MedicineType.PILL) }
    var instructions by remember { mutableStateOf("") }

    LaunchedEffect(medicineId) {
        if (medicineId != -1L) {
            viewModel.loadMedicineById(medicineId)
        }
    }
    
    LaunchedEffect(state.selectedMedicine) {
        if (medicineId != -1L && state.selectedMedicine != null) {
            name = state.selectedMedicine!!.name
            dosage = state.selectedMedicine!!.dosage
            type = state.selectedMedicine!!.type
            instructions = state.selectedMedicine!!.instructions
        }
    }

    LaunchedEffect(key1 = true) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is UiEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                is UiEvent.NavigateBack -> {
                    onNavigateBack()
                }
                else -> Unit
            }
        }
    }

    Scaffold(
        topBar = { 
            StandardTopAppBar(
                title = if (medicineId == -1L) "Add Medicine" else "Edit Medicine",
                showBackArrow = true,
                onBackClick = onNavigateBack
            ) 
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Medicine Name") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = dosage,
                onValueChange = { dosage = it },
                label = { Text("Dosage") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = instructions,
                onValueChange = { instructions = it },
                label = { Text("Instructions") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    val med = MedicineEntity(
                        id = if (medicineId == -1L) 0 else medicineId,
                        name = name,
                        dosage = dosage,
                        type = type,
                        instructions = instructions,
                        startDate = System.currentTimeMillis(),
                        endDate = null,
                        frequency = "Daily",
                        frequencyPattern = "Every 1 day",
                        reminderTimes = listOf("08:00"),
                        isActive = true
                    )
                    if (medicineId == -1L) {
                        viewModel.onAction(UiAction.AddMedicine(med))
                    } else {
                        viewModel.onAction(UiAction.UpdateMedicine(med))
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Save Medicine")
            }
        }
    }
}
