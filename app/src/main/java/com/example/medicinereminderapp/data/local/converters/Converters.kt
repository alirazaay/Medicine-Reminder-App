package com.example.medicinereminderapp.data.local.converters

import androidx.room.TypeConverter
import com.example.medicinereminderapp.domain.model.LogStatus
import com.example.medicinereminderapp.domain.model.MedicineType
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromMedicineType(value: MedicineType): String = value.name

    @TypeConverter
    fun toMedicineType(value: String): MedicineType = enumValueOf(value)

    @TypeConverter
    fun fromLogStatus(value: LogStatus): String = value.name

    @TypeConverter
    fun toLogStatus(value: String): LogStatus = enumValueOf(value)

    @TypeConverter
    fun fromStringList(value: List<String>): String = gson.toJson(value)

    @TypeConverter
    fun toStringList(value: String): List<String> {
        val listType = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, listType) ?: emptyList()
    }
}
