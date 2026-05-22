package com.example.medicinereminderapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.medicinereminderapp.data.local.AppDatabase
import com.example.medicinereminderapp.data.repository.MedicineRepositoryImpl
import com.example.medicinereminderapp.presentation.navigation.AppNavGraph
import com.example.medicinereminderapp.presentation.viewmodel.MedicineViewModel
import com.example.medicinereminderapp.presentation.viewmodel.ReminderLogViewModel
import com.example.medicinereminderapp.presentation.viewmodel.ViewModelFactory
import com.example.medicinereminderapp.data.scheduler.ReminderSchedulerImpl
import com.example.medicinereminderapp.ui.theme.MedicineReminderAppTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val database = AppDatabase.getDatabase(this)
        val repository = MedicineRepositoryImpl(database.medicineDao, database.reminderLogDao)
        val scheduler = ReminderSchedulerImpl(this)
        val factory = ViewModelFactory(repository, scheduler)
        
        val medicineViewModel: MedicineViewModel by viewModels { factory }
        val reminderLogViewModel: ReminderLogViewModel by viewModels { factory }

        setContent {
            MedicineReminderAppTheme {
                val navController = rememberNavController()
                val snackbarHostState = remember { SnackbarHostState() }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    snackbarHost = { SnackbarHost(snackbarHostState) }
                ) { innerPadding ->
                    androidx.compose.foundation.layout.Box(modifier = Modifier.padding(innerPadding)) {
                        AppNavGraph(
                            navController = navController,
                            medicineViewModel = medicineViewModel,
                            reminderLogViewModel = reminderLogViewModel,
                            snackbarHostState = snackbarHostState
                        )
                    }
                }
            }
        }
    }
}