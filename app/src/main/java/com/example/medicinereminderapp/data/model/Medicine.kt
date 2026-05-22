package com.example.medicinereminderapp.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "medicines")
data class Medicine(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val dosage: String,
    val type: MedicineType,
    val instructions: String,
    val startDate: Long,
    val endDate: Long?,
    val frequency: String, // "DAILY", "SPECIFIC_DAYS", "INTERVAL_DAYS"
    val frequencyPattern: String?, // "Monday,Wednesday,Friday" or interval digits like "3"
    val reminderTimes: List<String>, // List of times in "HH:mm" format, e.g., ["08:00", "20:00"]
    val isActive: Boolean = true
)
