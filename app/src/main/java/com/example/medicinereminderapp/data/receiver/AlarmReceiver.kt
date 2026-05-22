package com.example.medicinereminderapp.data.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.medicinereminderapp.data.local.AppDatabase
import com.example.medicinereminderapp.data.local.entity.ReminderLogEntity
import com.example.medicinereminderapp.data.notification.NotificationHelper
import com.example.medicinereminderapp.data.scheduler.ReminderSchedulerImpl
import com.example.medicinereminderapp.domain.model.LogStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val medicineId = intent.getLongExtra("MEDICINE_ID", -1L)
        val medicineName = intent.getStringExtra("MEDICINE_NAME") ?: "Unknown"
        val dosage = intent.getStringExtra("DOSAGE") ?: ""
        val notificationId = medicineId.toInt()

        if (medicineId != -1L) {
            val database = AppDatabase.getDatabase(context)
            
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val medicine = database.medicineDao.getMedicineById(medicineId)
                    if (medicine != null && medicine.isActive) {
                        val log = ReminderLogEntity(
                            medicineId = medicineId,
                            medicineName = medicine.name,
                            dosage = medicine.dosage,
                            scheduledDateTime = System.currentTimeMillis(),
                            status = LogStatus.PENDING
                        )
                        val logId = database.reminderLogDao.insertLog(log)

                        val notificationHelper = NotificationHelper(context)
                        notificationHelper.showReminderNotification(notificationId, logId, medicineName, dosage)

                        val scheduler = ReminderSchedulerImpl(context)
                        try {
                            scheduler.scheduleReminder(medicine)
                        } catch (e: SecurityException) {
                            Log.e("AlarmReceiver", "Failed to schedule next exact alarm for ${medicine.name}", e)
                        }
                    }
                } catch (e: Exception) {
                    Log.e("AlarmReceiver", "Error processing alarm", e)
                }
            }
        }
    }
}
