package com.example.medicinereminderapp.data.repository

import com.example.medicinereminderapp.data.model.Medicine
import com.example.medicinereminderapp.data.model.ReminderLog
import kotlinx.coroutines.flow.Flow

interface MedicineRepository {
    // Medicine operations
    fun getAllMedicines(): Flow<List<Medicine>>
    fun getActiveMedicinesFlow(): Flow<List<Medicine>>
    suspend fun getActiveMedicines(): List<Medicine>
    suspend fun getMedicineById(id: Long): Medicine?
    fun getMedicineByIdFlow(id: Long): Flow<Medicine?>
    suspend fun insertMedicine(medicine: Medicine): Long
    suspend fun updateMedicine(medicine: Medicine)
    suspend fun deleteMedicine(medicine: Medicine)

    // Log operations
    fun getAllLogsFlow(): Flow<List<ReminderLog>>
    fun getLogsForMedicine(medicineId: Long): Flow<List<ReminderLog>>
    fun getLogsForDateRange(start: Long, end: Long): Flow<List<ReminderLog>>
    suspend fun getLogsForDateRangeSuspend(start: Long, end: Long): List<ReminderLog>
    suspend fun insertLog(log: ReminderLog): Long
    suspend fun updateLog(log: ReminderLog)
    suspend fun deleteLog(log: ReminderLog)
    suspend fun getLogForScheduledTime(medicineId: Long, scheduledTime: Long): ReminderLog?
}
