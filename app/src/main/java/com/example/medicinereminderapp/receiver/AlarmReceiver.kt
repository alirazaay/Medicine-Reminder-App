package com.example.medicinereminderapp.receiver

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.medicinereminderapp.data.local.db.AppDatabase
import com.example.medicinereminderapp.data.model.LogStatus
import com.example.medicinereminderapp.data.model.ReminderLog
import com.example.medicinereminderapp.data.repository.MedicineRepositoryImpl
import com.example.medicinereminderapp.service.NotificationHelper
import com.example.medicinereminderapp.util.AlarmScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlarmReceiver : BroadcastReceiver() {
    private val TAG = "AlarmReceiver"
    private val scope = CoroutineScope(Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        val medicineId = intent.getLongExtra("MEDICINE_ID", -1L)
        val medicineName = intent.getStringExtra("MEDICINE_NAME") ?: "Medicine"
        val dosage = intent.getStringExtra("MEDICINE_DOSAGE") ?: "Dose"
        val instructions = intent.getStringExtra("MEDICINE_INSTRUCTIONS") ?: "Take as prescribed"
        val reminderTime = intent.getStringExtra("REMINDER_TIME") ?: ""
        val scheduledTimeMs = intent.getLongExtra("SCHEDULED_TIME_MS", System.currentTimeMillis())
        val notificationId = intent.getIntExtra("NOTIFICATION_ID", -1)

        val database = AppDatabase.getDatabase(context.applicationContext)
        val repository = MedicineRepositoryImpl(database.medicineDao(), database.reminderLogDao())

        Log.d(TAG, "onReceive: action=$action, medicineId=$medicineId, medicineName=$medicineName")

        if (action == "com.example.medicinereminderapp.ACTION_TAKE" || action == "com.example.medicinereminderapp.ACTION_SKIP") {
            // Dismiss notification
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (notificationId != -1) {
                notificationManager.cancel(notificationId)
            }

            val status = if (action == "com.example.medicinereminderapp.ACTION_TAKE") LogStatus.TAKEN else LogStatus.SKIPPED

            scope.launch {
                try {
                    // Check if a log already exists for this scheduled time (avoid double insertion)
                    val existingLog = repository.getLogForScheduledTime(medicineId, scheduledTimeMs)
                    if (existingLog == null) {
                        val log = ReminderLog(
                            medicineId = medicineId,
                            medicineName = medicineName,
                            dosage = dosage,
                            scheduledDateTime = scheduledTimeMs,
                            status = status,
                            actionDateTime = System.currentTimeMillis()
                        )
                        repository.insertLog(log)
                        Log.d(TAG, "Logged medication status $status for $medicineName scheduled at $scheduledTimeMs")
                    } else {
                        val updatedLog = existingLog.copy(
                            status = status,
                            actionDateTime = System.currentTimeMillis()
                        )
                        repository.updateLog(updatedLog)
                        Log.d(TAG, "Updated existing log to status $status for $medicineName scheduled at $scheduledTimeMs")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error inserting/updating reminder log", e)
                }
            }
        } else {
            // This is the actual alarm firing to trigger notification
            if (medicineId != -1L) {
                NotificationHelper.showMedicineNotification(
                    context = context,
                    medicineId = medicineId,
                    medicineName = medicineName,
                    dosage = dosage,
                    instructions = instructions,
                    reminderTime = reminderTime,
                    scheduledTimeMs = scheduledTimeMs
                )

                // Reschedule the NEXT instance of this alarm in the future
                scope.launch {
                    try {
                        val medicine = repository.getMedicineById(medicineId)
                        if (medicine != null && medicine.isActive) {
                            AlarmScheduler.scheduleAlarmsForMedicine(context, medicine)
                            
                            // Insert a PENDING log for this scheduled time so it shows up in dashboard agenda
                            val existingLog = repository.getLogForScheduledTime(medicineId, scheduledTimeMs)
                            if (existingLog == null) {
                                val pendingLog = ReminderLog(
                                    medicineId = medicineId,
                                    medicineName = medicine.name,
                                    dosage = medicine.dosage,
                                    scheduledDateTime = scheduledTimeMs,
                                    status = LogStatus.PENDING
                                )
                                repository.insertLog(pendingLog)
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error scheduling next alarm or creating pending log", e)
                    }
                }
            }
        }
    }
}
