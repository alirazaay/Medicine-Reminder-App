package com.example.medicinereminderapp.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.medicinereminderapp.data.local.entity.ReminderLogEntity
import com.example.medicinereminderapp.domain.model.LogStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderLogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: ReminderLogEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLogs(logs: List<ReminderLogEntity>)

    @Update
    suspend fun updateLog(log: ReminderLogEntity)

    @Delete
    suspend fun deleteLog(log: ReminderLogEntity)

    @Query("SELECT * FROM reminder_logs ORDER BY scheduledDateTime DESC")
    fun getAllLogs(): Flow<List<ReminderLogEntity>>

    @Query("SELECT * FROM reminder_logs WHERE id = :id")
    suspend fun getLogById(id: Long): ReminderLogEntity?

    @Query("SELECT * FROM reminder_logs WHERE scheduledDateTime >= :startOfDay AND scheduledDateTime <= :endOfDay ORDER BY scheduledDateTime ASC")
    fun getLogsForDay(startOfDay: Long, endOfDay: Long): Flow<List<ReminderLogEntity>>

    @Query("UPDATE reminder_logs SET status = :status, actionDateTime = :actionTime WHERE id = :logId")
    suspend fun updateLogStatus(logId: Long, status: LogStatus, actionTime: Long)
}
