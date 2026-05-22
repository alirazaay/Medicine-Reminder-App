package com.example.medicinereminderapp.presentation.event

import com.example.medicinereminderapp.data.local.entity.MedicineEntity
import com.example.medicinereminderapp.domain.model.LogStatus

sealed class UiAction {
    // Medicine Actions
    data class AddMedicine(val medicine: MedicineEntity) : UiAction()
    data class UpdateMedicine(val medicine: MedicineEntity) : UiAction()
    data class DeleteMedicine(val medicine: MedicineEntity) : UiAction()
    data class ToggleMedicineActiveStatus(val medicine: MedicineEntity) : UiAction()
    
    // Log Actions
    data class UpdateLogStatus(val logId: Long, val status: LogStatus, val actionTime: Long) : UiAction()
}
