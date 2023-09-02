package com.digital_tent.cow_marks.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Workshop::class], version = 1, exportSchema = false)
abstract class WorkshopDB : RoomDatabase() {
    abstract fun workshopDao(): WorkshopDao

    companion object {
        @Volatile
        private var INSTANCE: WorkshopDB? = null
        fun getDB(context: Context): WorkshopDB {
            val tempInstant = INSTANCE
            if(tempInstant != null) {
                return tempInstant
            }
            return Room.databaseBuilder(
                context.applicationContext,
                WorkshopDB::class.java,
                "workshop.db"
            ).build()
        }
    }
}