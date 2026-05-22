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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.medicinereminderapp.data.local.AppDatabase
import com.example.medicinereminderapp.data.repository.MedicineRepositoryImpl
import com.example.medicinereminderapp.data.repository.SettingsRepositoryImpl
import com.example.medicinereminderapp.data.repository.dataStore
import com.example.medicinereminderapp.presentation.navigation.AppNavGraph
import com.example.medicinereminderapp.presentation.viewmodel.MedicineViewModel
import com.example.medicinereminderapp.presentation.viewmodel.ReminderLogViewModel
import com.example.medicinereminderapp.presentation.viewmodel.SettingsViewModel
import com.example.medicinereminderapp.presentation.viewmodel.ViewModelFactory
import com.example.medicinereminderapp.data.scheduler.ReminderSchedulerImpl
import com.example.medicinereminderapp.ui.theme.MedicineReminderAppTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val database = AppDatabase.getDatabase(this)
        val repository = MedicineRepositoryImpl(database.medicineDao, database.reminderLogDao)
        val scheduler = ReminderSchedulerImpl(this)
        val settingsRepository = SettingsRepositoryImpl(applicationContext.dataStore)
        val factory = ViewModelFactory(repository, scheduler, settingsRepository)
        
        val medicineViewModel: MedicineViewModel by viewModels { factory }
        val reminderLogViewModel: ReminderLogViewModel by viewModels { factory }
        val settingsViewModel: SettingsViewModel by viewModels { factory }

        setContent {
            val settings by settingsViewModel.settings.collectAsState()
            
            MedicineReminderAppTheme(darkTheme = settings.isDarkMode) {
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
                            settingsViewModel = settingsViewModel,
                            snackbarHostState = snackbarHostState
                        )
                    }
                }
            }
        }
    }
}