package com.example.medicinereminderapp.domain.scheduler

import com.example.medicinereminderapp.data.local.entity.MedicineEntity

interface ReminderScheduler {
    fun scheduleReminder(medicine: MedicineEntity)
    fun cancelReminder(medicineId: Long)
}
