package com.example.medicinereminderapp.presentation.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.medicinereminderapp.presentation.ui.components.EmptyStateView
import com.example.medicinereminderapp.presentation.ui.components.MedicineCard
import com.example.medicinereminderapp.presentation.ui.components.StandardTopAppBar
import com.example.medicinereminderapp.presentation.viewmodel.MedicineViewModel

@Composable
fun HomeScreen(
    viewModel: MedicineViewModel,
    onNavigateToAddEdit: () -> Unit,
    onNavigateToDetail: (Long) -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = { StandardTopAppBar(title = "Medicines") },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToAddEdit) {
                Icon(Icons.Default.Add, contentDescription = "Add Medicine")
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(androidx.compose.ui.Alignment.Center))
            } else if (state.medicines.isEmpty()) {
                EmptyStateView(message = "No medicines added yet.")
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp)
                ) {
                    items(state.medicines) { medicine ->
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
