package com.example.medicinereminderapp.domain.repository

import com.example.medicinereminderapp.data.local.entity.MedicineEntity
import com.example.medicinereminderapp.data.local.entity.ReminderLogEntity
import com.example.medicinereminderapp.domain.model.LogStatus
import kotlinx.coroutines.flow.Flow

interface MedicineRepository {
    // Medicine
    suspend fun insertMedicine(medicine: MedicineEntity): Long
    suspend fun updateMedicine(medicine: MedicineEntity)
    suspend fun deleteMedicine(medicine: MedicineEntity)
    fun getAllMedicines(): Flow<List<MedicineEntity>>
    suspend fun getMedicineById(id: Long): MedicineEntity?
    fun getMedicineByIdFlow(id: Long): Flow<MedicineEntity?>
    fun getActiveMedicines(): Flow<List<MedicineEntity>>
    suspend fun getActiveMedicinesList(): List<MedicineEntity>

    // Reminder Logs
    suspend fun insertReminderLog(log: ReminderLogEntity): Long
    suspend fun insertReminderLogs(logs: List<ReminderLogEntity>)
    suspend fun updateReminderLog(log: ReminderLogEntity)
    suspend fun deleteReminderLog(log: ReminderLogEntity)
    fun getAllReminderLogs(): Flow<List<ReminderLogEntity>>
    suspend fun getReminderLogById(id: Long): ReminderLogEntity?
    fun getReminderLogsForDay(startOfDay: Long, endOfDay: Long): Flow<List<ReminderLogEntity>>
    suspend fun updateLogStatus(logId: Long, status: LogStatus, actionTime: Long)
    fun getLogsForMedicine(medicineId: Long): Flow<List<ReminderLogEntity>>
    suspend fun getLogForScheduledTime(medicineId: Long, scheduledTime: Long): ReminderLogEntity?
}
