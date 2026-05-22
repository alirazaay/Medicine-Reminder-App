package com.example.medicinereminderapp.ui.navigation

sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object AddEditMedicine : Screen("add_edit_medicine?medicineId={medicineId}") {
        fun passId(medicineId: Long = -1L): String = "add_edit_medicine?medicineId=$medicineId"
    }
    object Detail : Screen("detail/{medicineId}") {
        fun passId(medicineId: Long): String = "detail/$medicineId"
    }
    object History : Screen("history")
    object Settings : Screen("settings")
}
