package com.example.medicinereminderapp.presentation.ui.log

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.medicinereminderapp.presentation.ui.components.EmptyStateView
import com.example.medicinereminderapp.presentation.ui.components.ReminderLogCard
import com.example.medicinereminderapp.presentation.ui.components.StandardTopAppBar
import com.example.medicinereminderapp.presentation.viewmodel.ReminderLogViewModel

@Composable
fun ReminderLogScreen(
    viewModel: ReminderLogViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            StandardTopAppBar(
                title = "Reminder Logs",
                showBackArrow = true,
                onBackClick = onNavigateBack
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(androidx.compose.ui.Alignment.Center))
            } else if (state.logs.isEmpty()) {
                EmptyStateView(message = "No reminder logs found.")
            } else {
                LazyColumn(contentPadding = PaddingValues(16.dp)) {
                    items(state.logs) { log ->
                        ReminderLogCard(log = log)
                    }
                }
            }
        }
    }
}
