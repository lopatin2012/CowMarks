package com.digital_tent.cow_marks.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Code::class], version = 1, exportSchema = false)
abstract class CodeDB : RoomDatabase() {
    abstract fun codeDao(): CodeDao

    companion object {
        @Volatile
        private var INSTANCE: CodeDB? = null


        fun getDB(context: Context): CodeDB {
            val tempInstant = INSTANCE
            if(tempInstant != null) {
                return tempInstant
            }
            return Room.databaseBuilder(
                context.applicationContext,
                CodeDB::class.java,
                "codes.db"
            ).build()
        }
    }
}