package com.example.medicinereminderapp.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.medicinereminderapp.MainActivity
import com.example.medicinereminderapp.receiver.AlarmReceiver

object NotificationHelper {
    private const val CHANNEL_ID = "medicine_reminder_channel"
    private const val CHANNEL_NAME = "Medicine Reminders"
    private const val CHANNEL_DESC = "Notifications for scheduled medicine reminders"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESC
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 250, 500)
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showMedicineNotification(
        context: Context,
        medicineId: Long,
        medicineName: String,
        dosage: String,
        instructions: String,
        reminderTime: String,
        scheduledTimeMs: Long
    ) {
        // Create intent to open MainActivity when clicked
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("NAVIGATE_TO", "dashboard")
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            medicineId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // "Mark as Taken" action intent
        val takeIntent = Intent(context, AlarmReceiver::class.java).apply {
            action = "com.example.medicinereminderapp.ACTION_TAKE"
            putExtra("MEDICINE_ID", medicineId)
            putExtra("MEDICINE_NAME", medicineName)
            putExtra("MEDICINE_DOSAGE", dosage)
            putExtra("SCHEDULED_TIME_MS", scheduledTimeMs)
            putExtra("NOTIFICATION_ID", medicineId.toInt())
        }
        val takePendingIntent = PendingIntent.getBroadcast(
            context,
            medicineId.toInt() + 1000,
            takeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // "Skip" action intent
        val skipIntent = Intent(context, AlarmReceiver::class.java).apply {
            action = "com.example.medicinereminderapp.ACTION_SKIP"
            putExtra("MEDICINE_ID", medicineId)
            putExtra("MEDICINE_NAME", medicineName)
            putExtra("MEDICINE_DOSAGE", dosage)
            putExtra("SCHEDULED_TIME_MS", scheduledTimeMs)
            putExtra("NOTIFICATION_ID", medicineId.toInt())
        }
        val skipPendingIntent = PendingIntent.getBroadcast(
            context,
            medicineId.toInt() + 2000,
            skipIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        // Use Android's standard drawable resource (e.g. android.R.drawable.ic_lock_idle_alarm)
        // so that we don't have compilation errors due to missing vector assets.
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("Time for your medicine!")
            .setContentText("Take $medicineName ($dosage) - $instructions")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setSound(soundUri)
            .setVibrate(longArrayOf(0, 500, 250, 500))
            .setAutoCancel(true)
            .addAction(android.R.drawable.checkbox_on_background, "Taken", takePendingIntent)
            .addAction(android.R.drawable.ic_delete, "Skip", skipPendingIntent)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        // Use medicineId as notification ID to prevent duplicates for the same medicine
        notificationManager.notify(medicineId.toInt(), builder.build())
    }
}
