package com.example.medicinereminderapp.data.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.medicinereminderapp.data.local.AppDatabase
import com.example.medicinereminderapp.data.scheduler.ReminderSchedulerImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val database = AppDatabase.getDatabase(context)
            val scheduler = ReminderSchedulerImpl(context)

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val activeMedicines = database.medicineDao.getActiveMedicines().firstOrNull()
                    activeMedicines?.forEach { medicine ->
                        try {
                            scheduler.scheduleReminder(medicine)
                        } catch (e: SecurityException) {
                            Log.e("BootReceiver", "Failed to schedule exact alarm for ${medicine.name}", e)
                        }
                    }
                } catch (e: Exception) {
                    Log.e("BootReceiver", "Error during boot rescheduling", e)
                }
            }
        }
    }
}
