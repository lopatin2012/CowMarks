package com.digital_tent.cow_marks.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface WorkshopDao {

    @Insert
    suspend fun addWorkshop(workshop: Workshop)

    // Получить все цеха
    @Query("SELECT workshop FROM workshops")
    suspend fun getWorkshops(): List<String>

    // Получить список линий по цеху
    @Query("SELECT line FROM workshops WHERE workshop = :workshop")
    suspend fun getLineByWorkshop(workshop: String): List<String>

    @Query("DELETE FROM workshops")
    fun deleteBase()
}