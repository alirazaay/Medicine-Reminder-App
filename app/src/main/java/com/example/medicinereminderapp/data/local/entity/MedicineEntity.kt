package com.example.medicinereminderapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.medicinereminderapp.domain.model.MedicineType

@Entity(tableName = "medicines")
data class MedicineEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val dosage: String,
    val type: MedicineType,
    val instructions: String,
    val startDate: Long,
    val endDate: Long?,
    val frequency: String,
    val frequencyPattern: String?,
    val reminderTimes: List<String>,
    val isActive: Boolean = true
)
