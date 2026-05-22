package com.example.medicinereminderapp.data.scheduler

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.medicinereminderapp.data.local.entity.MedicineEntity
import com.example.medicinereminderapp.data.receiver.AlarmReceiver
import com.example.medicinereminderapp.domain.scheduler.ReminderScheduler
import java.util.Calendar

class ReminderSchedulerImpl(
    private val context: Context
) : ReminderScheduler {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    override fun scheduleReminder(medicine: MedicineEntity) {
        cancelReminder(medicine.id)

        if (!medicine.isActive || medicine.reminderTimes.isEmpty()) {
            return
        }

        val now = System.currentTimeMillis()
        var nextAlarmTime = Long.MAX_VALUE

        medicine.reminderTimes.forEach { timeStr ->
            val parts = timeStr.split(":")
            if (parts.size == 2) {
                val hour = parts[0].toIntOrNull() ?: return@forEach
                val minute = parts[1].toIntOrNull() ?: return@forEach

                val calendar = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, hour)
                    set(Calendar.MINUTE, minute)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }

                if (calendar.timeInMillis <= now) {
                    calendar.add(Calendar.DAY_OF_YEAR, 1)
                }

                if (calendar.timeInMillis < nextAlarmTime) {
                    nextAlarmTime = calendar.timeInMillis
                }
            }
        }

        if (nextAlarmTime != Long.MAX_VALUE) {
            val intent = Intent(context, AlarmReceiver::class.java).apply {
                putExtra("MEDICINE_ID", medicine.id)
                putExtra("MEDICINE_NAME", medicine.name)
                putExtra("DOSAGE", medicine.dosage)
            }

            val requestCode = medicine.id.toInt()

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                throw SecurityException("Exact alarm permission is missing. Cannot schedule medicine reminders safely.")
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    nextAlarmTime,
                    pendingIntent
                )
            }
        }
    }

    override fun cancelReminder(medicineId: Long) {
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            medicineId.toInt(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }
}
