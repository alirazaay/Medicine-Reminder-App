package com.example.medicinereminderapp.data.repository

import com.example.medicinereminderapp.data.local.db.dao.MedicineDao
import com.example.medicinereminderapp.data.local.db.dao.ReminderLogDao
import com.example.medicinereminderapp.data.model.Medicine
import com.example.medicinereminderapp.data.model.ReminderLog
import kotlinx.coroutines.flow.Flow

class MedicineRepositoryImpl(
    private val medicineDao: MedicineDao,
    private val reminderLogDao: ReminderLogDao
) : MedicineRepository {

    override fun getAllMedicines(): Flow<List<Medicine>> = medicineDao.getAllMedicines()

    override fun getActiveMedicinesFlow(): Flow<List<Medicine>> = medicineDao.getActiveMedicinesFlow()

    override suspend fun getActiveMedicines(): List<Medicine> = medicineDao.getActiveMedicines()

    override suspend fun getMedicineById(id: Long): Medicine? = medicineDao.getMedicineById(id)

    override fun getMedicineByIdFlow(id: Long): Flow<Medicine?> = medicineDao.getMedicineByIdFlow(id)

    override suspend fun insertMedicine(medicine: Medicine): Long = medicineDao.insertMedicine(medicine)

    override suspend fun updateMedicine(medicine: Medicine) = medicineDao.updateMedicine(medicine)

    override suspend fun deleteMedicine(medicine: Medicine) = medicineDao.deleteMedicine(medicine)

    override fun getAllLogsFlow(): Flow<List<ReminderLog>> = reminderLogDao.getAllLogsFlow()

    override fun getLogsForMedicine(medicineId: Long): Flow<List<ReminderLog>> = reminderLogDao.getLogsForMedicine(medicineId)

    override fun getLogsForDateRange(start: Long, end: Long): Flow<List<ReminderLog>> = reminderLogDao.getLogsForDateRange(start, end)

    override suspend fun getLogsForDateRangeSuspend(start: Long, end: Long): List<ReminderLog> = reminderLogDao.getLogsForDateRangeSuspend(start, end)

    override suspend fun insertLog(log: ReminderLog): Long = reminderLogDao.insertLog(log)

    override suspend fun updateLog(log: ReminderLog) = reminderLogDao.updateLog(log)

    override suspend fun deleteLog(log: ReminderLog) = reminderLogDao.deleteLog(log)

    override suspend fun getLogForScheduledTime(medicineId: Long, scheduledTime: Long): ReminderLog? = reminderLogDao.getLogForScheduledTime(medicineId, scheduledTime)
}
