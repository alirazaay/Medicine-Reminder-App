package com.example.medicinereminderapp.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.medicinereminderapp.data.local.db.AppDatabase
import com.example.medicinereminderapp.data.repository.MedicineRepositoryImpl
import com.example.medicinereminderapp.util.AlarmScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    private val TAG = "BootReceiver"
    private val scope = CoroutineScope(Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == "android.intent.action.QUICKBOOT_POWERON") {
            Log.d(TAG, "Reboot detected. Rescheduling alarms...")
            val database = AppDatabase.getDatabase(context.applicationContext)
            val repository = MedicineRepositoryImpl(database.medicineDao(), database.reminderLogDao())

            scope.launch {
                try {
                    val activeMedicines = repository.getActiveMedicines()
                    activeMedicines.forEach { medicine ->
                        AlarmScheduler.scheduleAlarmsForMedicine(context, medicine)
                    }
                    Log.d(TAG, "Successfully rescheduled alarms for ${activeMedicines.size} active medicines.")
                } catch (e: Exception) {
                    Log.e(TAG, "Error rescheduling alarms on boot", e)
                }
            }
        }
    }
}
