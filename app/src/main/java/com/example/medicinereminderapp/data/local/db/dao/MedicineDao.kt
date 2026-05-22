package com.example.medicinereminderapp.data.local.db.dao

import androidx.room.*
import com.example.medicinereminderapp.data.model.Medicine
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicineDao {

    @Query("SELECT * FROM medicines ORDER BY name ASC")
    fun getAllMedicines(): Flow<List<Medicine>>

    @Query("SELECT * FROM medicines WHERE isActive = 1")
    fun getActiveMedicinesFlow(): Flow<List<Medicine>>

    @Query("SELECT * FROM medicines WHERE isActive = 1")
    suspend fun getActiveMedicines(): List<Medicine>

    @Query("SELECT * FROM medicines WHERE id = :id")
    suspend fun getMedicineById(id: Long): Medicine?

    @Query("SELECT * FROM medicines WHERE id = :id")
    fun getMedicineByIdFlow(id: Long): Flow<Medicine?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedicine(medicine: Medicine): Long

    @Update
    suspend fun updateMedicine(medicine: Medicine)

    @Delete
    suspend fun deleteMedicine(medicine: Medicine)
}
