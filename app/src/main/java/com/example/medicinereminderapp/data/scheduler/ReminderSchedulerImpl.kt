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
        if (!medicine.isActive) {
            cancelReminder(medicine.id)
            return
        }

        medicine.reminderTimes.forEachIndexed { index, timeStr ->
            val parts = timeStr.split(":")
            if (parts.size == 2) {
                val hour = parts[0].toIntOrNull() ?: return@forEachIndexed
                val minute = parts[1].toIntOrNull() ?: return@forEachIndexed

                val calendar = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, hour)
                    set(Calendar.MINUTE, minute)
                    set(Calendar.SECOND, 0)
                }

                if (calendar.timeInMillis <= System.currentTimeMillis()) {
                    calendar.add(Calendar.DAY_OF_YEAR, 1)
                }

                val intent = Intent(context, AlarmReceiver::class.java).apply {
                    putExtra("MEDICINE_ID", medicine.id)
                    putExtra("MEDICINE_NAME", medicine.name)
                    putExtra("DOSAGE", medicine.dosage)
                }

                val requestCode = (medicine.id.toInt() * 100) + index

                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    requestCode,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                    alarmManager.set(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                }
            }
        }
    }

    override fun cancelReminder(medicineId: Long) {
        for (index in 0..10) {
            val requestCode = (medicineId.toInt() * 100) + index
            val intent = Intent(context, AlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent)
                pendingIntent.cancel()
            }
        }
    }
}
