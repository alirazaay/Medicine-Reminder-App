package com.example.medicinereminderapp.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.medicinereminderapp.data.local.converters.Converters
import com.example.medicinereminderapp.data.local.dao.MedicineDao
import com.example.medicinereminderapp.data.local.dao.ReminderLogDao
import com.example.medicinereminderapp.data.local.entity.MedicineEntity
import com.example.medicinereminderapp.data.local.entity.ReminderLogEntity

@Database(
    entities = [MedicineEntity::class, ReminderLogEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract val medicineDao: MedicineDao
    abstract val reminderLogDao: ReminderLogDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "medicine_reminder_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
