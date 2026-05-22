package com.example.medicinereminderapp.presentation.ui.detail

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.medicinereminderapp.presentation.ui.components.StandardTopAppBar
import com.example.medicinereminderapp.presentation.viewmodel.MedicineViewModel

@Composable
fun MedicineDetailScreen(
    viewModel: MedicineViewModel,
    medicineId: Long,
    onNavigateBack: () -> Unit,
    onEditClick: (Long) -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    
    LaunchedEffect(medicineId) {
        viewModel.loadMedicineById(medicineId)
    }

    Scaffold(
        topBar = {
            StandardTopAppBar(
                title = "Medicine Details",
                showBackArrow = true,
                onBackClick = onNavigateBack
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(androidx.compose.ui.Alignment.Center))
            } else if (state.selectedMedicine != null) {
                val medicine = state.selectedMedicine!!
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Name: ${medicine.name}", style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Dosage: ${medicine.dosage}", style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Type: ${medicine.type.name}", style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Instructions: ${medicine.instructions}", style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { onEditClick(medicineId) }) {
                        Text(text = "Edit Medicine")
                    }
                }
            } else {
                Text(text = "Medicine not found.", modifier = Modifier.align(androidx.compose.ui.Alignment.Center))
            }
        }
    }
}
