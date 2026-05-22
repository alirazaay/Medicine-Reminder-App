package com.example.medicinereminderapp.data.receiver

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.medicinereminderapp.data.local.AppDatabase
import com.example.medicinereminderapp.domain.model.LogStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotificationActionReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_TAKEN = "com.example.medicinereminderapp.ACTION_TAKEN"
        const val ACTION_SKIP = "com.example.medicinereminderapp.ACTION_SKIP"
        const val EXTRA_LOG_ID = "extra_log_id"
        const val EXTRA_NOTIFICATION_ID = "extra_notification_id"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val logId = intent.getLongExtra(EXTRA_LOG_ID, -1L)
        val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1)
        val action = intent.action

        if (logId != -1L && notificationId != -1) {
            val status = when (action) {
                ACTION_TAKEN -> LogStatus.TAKEN
                ACTION_SKIP -> LogStatus.SKIPPED
                else -> return
            }

            // Dismiss the notification
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(notificationId)

            // Update the log status in the database
            val database = AppDatabase.getDatabase(context)
            CoroutineScope(Dispatchers.IO).launch {
                database.reminderLogDao.updateLogStatus(
                    logId = logId,
                    status = status,
                    actionTime = System.currentTimeMillis()
                )
            }
        }
    }
}
