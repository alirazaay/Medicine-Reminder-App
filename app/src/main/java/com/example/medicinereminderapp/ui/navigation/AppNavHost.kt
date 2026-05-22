package com.example.medicinereminderapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.medicinereminderapp.ui.screen.addedit.AddEditScreen
import com.example.medicinereminderapp.ui.screen.addedit.AddEditViewModel
import com.example.medicinereminderapp.ui.screen.dashboard.DashboardScreen
import com.example.medicinereminderapp.ui.screen.dashboard.DashboardViewModel
import com.example.medicinereminderapp.ui.screen.detail.DetailScreen
import com.example.medicinereminderapp.ui.screen.detail.DetailViewModel
import com.example.medicinereminderapp.ui.screen.history.HistoryScreen
import com.example.medicinereminderapp.ui.screen.history.HistoryViewModel
import com.example.medicinereminderapp.ui.screen.settings.SettingsScreen
import com.example.medicinereminderapp.ui.screen.settings.SettingsViewModel

@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard.route
    ) {
        composable(route = Screen.Dashboard.route) {
            val viewModel: DashboardViewModel = viewModel()
            DashboardScreen(
                viewModel = viewModel,
                onNavigateToAddEdit = { medicineId ->
                    navController.navigate(Screen.AddEditMedicine.passId(medicineId))
                },
                onNavigateToDetails = { medicineId ->
                    navController.navigate(Screen.Detail.passId(medicineId))
                },
                onNavigateToHistory = {
                    navController.navigate(Screen.History.route)
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }

        composable(
            route = Screen.AddEditMedicine.route,
            arguments = listOf(
                navArgument("medicineId") {
                    type = NavType.LongType
                    defaultValue = -1L
                }
            )
        ) { backStackEntry ->
            val medicineId = backStackEntry.arguments?.getLong("medicineId") ?: -1L
            val viewModel: AddEditViewModel = viewModel()
            AddEditScreen(
                viewModel = viewModel,
                medicineId = medicineId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screen.Detail.route,
            arguments = listOf(
                navArgument("medicineId") {
                    type = NavType.LongType
                }
            )
        ) { backStackEntry ->
            val medicineId = backStackEntry.arguments?.getLong("medicineId") ?: -1L
            val viewModel: DetailViewModel = viewModel()
            DetailScreen(
                viewModel = viewModel,
                medicineId = medicineId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToEdit = { id ->
                    navController.navigate(Screen.AddEditMedicine.passId(id))
                }
            )
        }

        composable(route = Screen.History.route) {
            val viewModel: HistoryViewModel = viewModel()
            HistoryScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(route = Screen.Settings.route) {
            val viewModel: SettingsViewModel = viewModel()
            SettingsScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
