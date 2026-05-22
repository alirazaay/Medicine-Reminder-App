package com.example.medicinereminderapp.presentation.navigation

sealed class ScreenRoutes(val route: String) {
    object Home : ScreenRoutes("home_screen")
    object AddEditMedicine : ScreenRoutes("add_edit_medicine_screen?medicineId={medicineId}") {
        fun passMedicineId(medicineId: Long?): String {
            return "add_edit_medicine_screen?medicineId=${medicineId ?: -1L}"
        }
    }
    object MedicineDetail : ScreenRoutes("medicine_detail_screen/{medicineId}") {
        fun passMedicineId(medicineId: Long): String {
            return "medicine_detail_screen/$medicineId"
        }
    }
    object ReminderLog : ScreenRoutes("reminder_log_screen")
}
