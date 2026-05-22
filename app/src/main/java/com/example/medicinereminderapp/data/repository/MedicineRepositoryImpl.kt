package com.example.medicinereminderapp.data.repository

import com.example.medicinereminderapp.data.local.dao.MedicineDao
import com.example.medicinereminderapp.data.local.dao.ReminderLogDao
import com.example.medicinereminderapp.data.local.entity.MedicineEntity
import com.example.medicinereminderapp.data.local.entity.ReminderLogEntity
import com.example.medicinereminderapp.domain.model.LogStatus
import com.example.medicinereminderapp.domain.repository.MedicineRepository
import kotlinx.coroutines.flow.Flow

class MedicineRepositoryImpl(
    private val medicineDao: MedicineDao,
    private val reminderLogDao: ReminderLogDao
) : MedicineRepository {

    // Medicine
    override suspend fun insertMedicine(medicine: MedicineEntity): Long =
        medicineDao.insertMedicine(medicine)

    override suspend fun updateMedicine(medicine: MedicineEntity) =
        medicineDao.updateMedicine(medicine)

    override suspend fun deleteMedicine(medicine: MedicineEntity) =
        medicineDao.deleteMedicine(medicine)

    override fun getAllMedicines(): Flow<List<MedicineEntity>> =
        medicineDao.getAllMedicines()

    override suspend fun getMedicineById(id: Long): MedicineEntity? =
        medicineDao.getMedicineById(id)

    override fun getMedicineByIdFlow(id: Long): Flow<MedicineEntity?> =
        medicineDao.getMedicineByIdFlow(id)

    override fun getActiveMedicines(): Flow<List<MedicineEntity>> =
        medicineDao.getActiveMedicines()

    override suspend fun getActiveMedicinesList(): List<MedicineEntity> =
        medicineDao.getActiveMedicinesList()

    // Reminder Logs
    override suspend fun insertReminderLog(log: ReminderLogEntity): Long =
        reminderLogDao.insertLog(log)

    override suspend fun insertReminderLogs(logs: List<ReminderLogEntity>) =
        reminderLogDao.insertLogs(logs)

    override suspend fun updateReminderLog(log: ReminderLogEntity) =
        reminderLogDao.updateLog(log)

    override suspend fun deleteReminderLog(log: ReminderLogEntity) =
        reminderLogDao.deleteLog(log)

    override fun getAllReminderLogs(): Flow<List<ReminderLogEntity>> =
        reminderLogDao.getAllLogs()

    override suspend fun getReminderLogById(id: Long): ReminderLogEntity? =
        reminderLogDao.getLogById(id)

    override fun getReminderLogsForDay(startOfDay: Long, endOfDay: Long): Flow<List<ReminderLogEntity>> =
        reminderLogDao.getLogsForDay(startOfDay, endOfDay)

    override suspend fun updateLogStatus(logId: Long, status: LogStatus, actionTime: Long) =
        reminderLogDao.updateLogStatus(logId, status, actionTime)

    override fun getLogsForMedicine(medicineId: Long): Flow<List<ReminderLogEntity>> =
        reminderLogDao.getLogsForMedicine(medicineId)

    override suspend fun getLogForScheduledTime(medicineId: Long, scheduledTime: Long): ReminderLogEntity? =
        reminderLogDao.getLogForScheduledTime(medicineId, scheduledTime)
}
