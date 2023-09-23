package com.digital_tent.cow_marks.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [CodeAll::class], version = 1, exportSchema = false)
abstract class CodeAllDB : RoomDatabase() {
    abstract fun codeAllDao(): CodeAllDao

    companion object {
        @Volatile
        private var INSTANCE: CodeAllDB? = null

        fun getDB(context: Context): CodeAllDB  {
            val tempInstant = INSTANCE
            if(tempInstant != null) {
                return tempInstant
            }
            return Room.databaseBuilder(
                context.applicationContext,
                CodeAllDB::class.java,
                "codes_all.db"
            ).build()
        }
    }
}