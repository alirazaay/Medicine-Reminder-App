package com.example.medicinereminderapp.data.local.db.dao

import androidx.room.*
import com.example.medicinereminderapp.data.model.ReminderLog
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderLogDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: ReminderLog): Long

    @Update
    suspend fun updateLog(log: ReminderLog)

    @Delete
    suspend fun deleteLog(log: ReminderLog)

    @Query("SELECT * FROM reminder_logs ORDER BY scheduledDateTime DESC")
    fun getAllLogsFlow(): Flow<List<ReminderLog>>

    @Query("SELECT * FROM reminder_logs WHERE medicineId = :medicineId ORDER BY scheduledDateTime DESC")
    fun getLogsForMedicine(medicineId: Long): Flow<List<ReminderLog>>

    @Query("SELECT * FROM reminder_logs WHERE scheduledDateTime >= :start AND scheduledDateTime <= :end ORDER BY scheduledDateTime ASC")
    fun getLogsForDateRange(start: Long, end: Long): Flow<List<ReminderLog>>

    @Query("SELECT * FROM reminder_logs WHERE scheduledDateTime >= :start AND scheduledDateTime <= :end ORDER BY scheduledDateTime ASC")
    suspend fun getLogsForDateRangeSuspend(start: Long, end: Long): List<ReminderLog>

    @Query("SELECT * FROM reminder_logs WHERE medicineId = :medicineId AND scheduledDateTime = :scheduledTime LIMIT 1")
    suspend fun getLogForScheduledTime(medicineId: Long, scheduledTime: Long): ReminderLog?
}
