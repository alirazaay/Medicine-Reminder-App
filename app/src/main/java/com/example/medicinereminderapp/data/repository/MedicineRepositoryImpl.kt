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

    override suspend fun insertMedicine(medicine: MedicineEntity): Long {
        return medicineDao.insertMedicine(medicine)
    }

    override suspend fun updateMedicine(medicine: MedicineEntity) {
        medicineDao.updateMedicine(medicine)
    }

    override suspend fun deleteMedicine(medicine: MedicineEntity) {
        medicineDao.deleteMedicine(medicine)
    }

    override fun getAllMedicines(): Flow<List<MedicineEntity>> {
        return medicineDao.getAllMedicines()
    }

    override suspend fun getMedicineById(id: Long): MedicineEntity? {
        return medicineDao.getMedicineById(id)
    }

    override fun getActiveMedicines(): Flow<List<MedicineEntity>> {
        return medicineDao.getActiveMedicines()
    }

    override suspend fun insertReminderLog(log: ReminderLogEntity): Long {
        return reminderLogDao.insertLog(log)
    }

    override suspend fun insertReminderLogs(logs: List<ReminderLogEntity>) {
        reminderLogDao.insertLogs(logs)
    }

    override suspend fun updateReminderLog(log: ReminderLogEntity) {
        reminderLogDao.updateLog(log)
    }

    override suspend fun deleteReminderLog(log: ReminderLogEntity) {
        reminderLogDao.deleteLog(log)
    }

    override fun getAllReminderLogs(): Flow<List<ReminderLogEntity>> {
        return reminderLogDao.getAllLogs()
    }

    override suspend fun getReminderLogById(id: Long): ReminderLogEntity? {
        return reminderLogDao.getLogById(id)
    }

    override fun getReminderLogsForDay(startOfDay: Long, endOfDay: Long): Flow<List<ReminderLogEntity>> {
        return reminderLogDao.getLogsForDay(startOfDay, endOfDay)
    }

    override suspend fun updateLogStatus(logId: Long, status: LogStatus, actionTime: Long) {
        reminderLogDao.updateLogStatus(logId, status, actionTime)
    }
}
