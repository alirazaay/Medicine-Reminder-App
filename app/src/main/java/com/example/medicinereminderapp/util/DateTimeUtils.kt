package com.example.medicinereminderapp.util

import com.example.medicinereminderapp.data.model.Medicine
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateTimeUtils {

    fun getTodayMillis(): Long {
        return getStartOfDay(System.currentTimeMillis())
    }

    fun getStartOfDay(millis: Long): Long {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = millis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return calendar.timeInMillis
    }

    fun getEndOfDay(millis: Long): Long {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = millis
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }
        return calendar.timeInMillis
    }

    fun formatDate(millis: Long): String {
        val sdf = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())
        return sdf.format(Date(millis))
    }

    fun formatShortDate(millis: Long): String {
        val sdf = SimpleDateFormat("dd MMM", Locale.getDefault())
        return sdf.format(Date(millis))
    }

    fun getDayOfWeekLetter(millis: Long): String {
        val sdf = SimpleDateFormat("EEEEE", Locale.getDefault()) // Single letter day, e.g. 'M', 'T', 'W'
        return sdf.format(Date(millis))
    }

    fun getDayOfWeekName(millis: Long): String {
        val sdf = SimpleDateFormat("EEEE", Locale.getDefault()) // Full day name, e.g. "Monday"
        return sdf.format(Date(millis))
    }

    fun getTimeInMillis(dateMillis: Long, timeStr: String): Long {
        val parts = timeStr.split(":")
        if (parts.size < 2) return dateMillis
        val hour = parts[0].toIntOrNull() ?: 0
        val minute = parts[1].toIntOrNull() ?: 0

        val calendar = Calendar.getInstance().apply {
            timeInMillis = dateMillis
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return calendar.timeInMillis
    }

    fun isScheduledOnDate(medicine: Medicine, dateMillis: Long): Boolean {
        val dateStart = getStartOfDay(dateMillis)
        
        // Ensure date falls within start and end date course
        if (dateStart < getStartOfDay(medicine.startDate)) {
            return false
        }
        
        medicine.endDate?.let { end ->
            if (dateStart > getStartOfDay(end)) {
                return false
            }
        }

        return when (medicine.frequency) {
            "DAILY" -> true
            "SPECIFIC_DAYS" -> {
                val dayOfWeekStr = getDayOfWeekName(dateMillis).lowercase()
                val activeDays = medicine.frequencyPattern?.split(",")?.map { it.trim().lowercase() } ?: emptyList()
                activeDays.contains(dayOfWeekStr)
            }
            "INTERVAL_DAYS" -> {
                val interval = medicine.frequencyPattern?.toIntOrNull() ?: 1
                val diffTime = dateStart - getStartOfDay(medicine.startDate)
                val diffDays = (diffTime / (1000 * 60 * 60 * 24)).toInt()
                diffDays >= 0 && diffDays % interval == 0
            }
            else -> true
        }
    }
}
