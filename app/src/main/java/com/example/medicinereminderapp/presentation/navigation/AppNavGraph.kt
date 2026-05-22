package com.example.medicinereminderapp.presentation.navigation

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.medicinereminderapp.presentation.ui.add_edit.AddEditMedicineScreen
import com.example.medicinereminderapp.presentation.ui.detail.MedicineDetailScreen
import com.example.medicinereminderapp.presentation.ui.home.HomeScreen
import com.example.medicinereminderapp.presentation.ui.log.ReminderLogScreen
import com.example.medicinereminderapp.presentation.ui.settings.SettingsScreen
import com.example.medicinereminderapp.presentation.viewmodel.MedicineViewModel
import com.example.medicinereminderapp.presentation.viewmodel.ReminderLogViewModel
import com.example.medicinereminderapp.presentation.viewmodel.SettingsViewModel

@Composable
fun AppNavGraph(
    navController: NavHostController,
    medicineViewModel: MedicineViewModel,
    reminderLogViewModel: ReminderLogViewModel,
    settingsViewModel: SettingsViewModel,
    snackbarHostState: SnackbarHostState
) {
    NavHost(
        navController = navController,
        startDestination = ScreenRoutes.Home.route
    ) {
        composable(route = ScreenRoutes.Home.route) {
            HomeScreen(
                medicineViewModel = medicineViewModel,
                reminderLogViewModel = reminderLogViewModel,
                onNavigateToAddEdit = {
                    navController.navigate(ScreenRoutes.AddEditMedicine.passMedicineId(-1L))
                },
                onNavigateToDetail = { id ->
                    navController.navigate(ScreenRoutes.MedicineDetail.passMedicineId(id))
                },
                onNavigateToSettings = {
                    navController.navigate(ScreenRoutes.Settings.route)
                }
            )
        }
        
        composable(
            route = ScreenRoutes.AddEditMedicine.route,
            arguments = listOf(
                navArgument("medicineId") {
                    type = NavType.LongType
                    defaultValue = -1L
                }
            )
        ) { backStackEntry ->
            val medicineId = backStackEntry.arguments?.getLong("medicineId") ?: -1L
            AddEditMedicineScreen(
                viewModel = medicineViewModel,
                medicineId = medicineId,
                onNavigateBack = { navController.popBackStack() },
                snackbarHostState = snackbarHostState
            )
        }

        composable(
            route = ScreenRoutes.MedicineDetail.route,
            arguments = listOf(
                navArgument("medicineId") {
                    type = NavType.LongType
                }
            )
        ) { backStackEntry ->
            val medicineId = backStackEntry.arguments?.getLong("medicineId") ?: return@composable
            MedicineDetailScreen(
                viewModel = medicineViewModel,
                medicineId = medicineId,
                onNavigateBack = { navController.popBackStack() },
                onEditClick = { id ->
                    navController.navigate(ScreenRoutes.AddEditMedicine.passMedicineId(id))
                }
            )
        }

        composable(route = ScreenRoutes.ReminderLog.route) {
            ReminderLogScreen(
                viewModel = reminderLogViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(route = ScreenRoutes.Settings.route) {
            SettingsScreen(
                viewModel = settingsViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
