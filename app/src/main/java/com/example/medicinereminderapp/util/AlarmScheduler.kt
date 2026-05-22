package com.example.medicinereminderapp.util

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.medicinereminderapp.data.local.entity.MedicineEntity
import com.example.medicinereminderapp.receiver.AlarmReceiver
import java.util.Calendar

object AlarmScheduler {
    private const val TAG = "AlarmScheduler"

    @SuppressLint("ScheduleExactAlarm")
    fun scheduleAlarmsForMedicine(context: Context, medicine: Medicine) {
        if (!medicine.isActive) {
            cancelAlarmsForMedicine(context, medicine)
            return
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        // In Android 12+, we need to check if we can schedule exact alarms
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                Log.w(TAG, "Cannot schedule exact alarms - permission not granted yet.")
                // Fallback to scheduling normal alarms or wait for permission
            }
        }

        medicine.reminderTimes.forEachIndexed { index, timeStr ->
            val triggerTime = calculateNextTriggerTime(medicine, timeStr)
            if (triggerTime > 0) {
                val intent = Intent(context, AlarmReceiver::class.java).apply {
                    putExtra("MEDICINE_ID", medicine.id)
                    putExtra("MEDICINE_NAME", medicine.name)
                    putExtra("MEDICINE_DOSAGE", medicine.dosage)
                    putExtra("MEDICINE_INSTRUCTIONS", medicine.instructions)
                    putExtra("REMINDER_TIME", timeStr)
                    putExtra("SCHEDULED_TIME_MS", triggerTime)
                }

                val requestCode = getUniqueRequestCode(medicine.id, index)
                
                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    requestCode,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            triggerTime,
                            pendingIntent
                        )
                    } else {
                        alarmManager.setExact(
                            AlarmManager.RTC_WAKEUP,
                            triggerTime,
                            pendingIntent
                        )
                    }
                    Log.d(TAG, "Scheduled alarm for ${medicine.name} at $timeStr (Trigger: $triggerTime, RequestCode: $requestCode)")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to schedule alarm for ${medicine.name}", e)
                }
            }
        }
    }

    fun cancelAlarmsForMedicine(context: Context, medicine: Medicine) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        medicine.reminderTimes.forEachIndexed { index, timeStr ->
            val intent = Intent(context, AlarmReceiver::class.java)
            val requestCode = getUniqueRequestCode(medicine.id, index)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent)
                pendingIntent.cancel()
                Log.d(TAG, "Cancelled alarm for ${medicine.name} at $timeStr (RequestCode: $requestCode)")
            }
        }
    }

    fun getUniqueRequestCode(medicineId: Long, timeIndex: Int): Int {
        return (medicineId.toInt() * 100) + timeIndex
    }

    fun calculateNextTriggerTime(medicine: Medicine, timeStr: String): Long {
        val timeParts = timeStr.split(":")
        if (timeParts.size < 2) return -1L
        val hour = timeParts[0].toIntOrNull() ?: return -1L
        val minute = timeParts[1].toIntOrNull() ?: return -1L

        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val now = System.currentTimeMillis()

        when (medicine.frequency) {
            "DAILY" -> {
                if (calendar.timeInMillis <= now) {
                    calendar.add(Calendar.DAY_OF_YEAR, 1)
                }
            }
            "SPECIFIC_DAYS" -> {
                val daysList = medicine.frequencyPattern?.split(",")?.map { it.trim().lowercase() } ?: emptyList()
                if (daysList.isEmpty()) return -1L
                
                var daysAdded = 0
                while (daysAdded < 8) {
                    val dayOfWeekStr = getDayOfWeekString(calendar.get(Calendar.DAY_OF_WEEK))
                    if (daysList.contains(dayOfWeekStr)) {
                        if (calendar.timeInMillis > now) {
                            break
                        }
                    }
                    calendar.add(Calendar.DAY_OF_YEAR, 1)
                    daysAdded++
                }
            }
            "INTERVAL_DAYS" -> {
                val interval = medicine.frequencyPattern?.toIntOrNull() ?: 1
                val startCal = Calendar.getInstance().apply { timeInMillis = medicine.startDate }
                
                val targetCal = Calendar.getInstance().apply {
                    timeInMillis = startCal.timeInMillis
                    set(Calendar.HOUR_OF_DAY, hour)
                    set(Calendar.MINUTE, minute)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }

                while (targetCal.timeInMillis <= now) {
                    targetCal.add(Calendar.DAY_OF_YEAR, interval)
                }
                calendar.timeInMillis = targetCal.timeInMillis
            }
            else -> {
                if (calendar.timeInMillis <= now) {
                    calendar.add(Calendar.DAY_OF_YEAR, 1)
                }
            }
        }

        medicine.endDate?.let { end ->
            if (calendar.timeInMillis > end) {
                return -1L // Exceeded end date course
            }
        }

        return calendar.timeInMillis
    }

    private fun getDayOfWeekString(dayOfWeek: Int): String {
        return when (dayOfWeek) {
            Calendar.SUNDAY -> "sunday"
            Calendar.MONDAY -> "monday"
            Calendar.TUESDAY -> "tuesday"
            Calendar.WEDNESDAY -> "wednesday"
            Calendar.THURSDAY -> "thursday"
            Calendar.FRIDAY -> "friday"
            Calendar.SATURDAY -> "saturday"
            else -> ""
        }
    }
}
