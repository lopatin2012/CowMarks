package com.digital_tent.cow_marks.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [CodesForPrinter::class], version = 1, exportSchema = false)
abstract class CodesForPrinterDB: RoomDatabase() {
    abstract fun CodesForPrinterDao(): CodesForPrinterDao

    companion object {
        @Volatile
        private var INSTANCE: CodesForPrinterDB? = null
        fun getDB(context: Context): CodesForPrinterDB {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }

            return Room.databaseBuilder(
                context.applicationContext,
                CodesForPrinterDB::class.java,
                "codes_for_printer.db"
            ).build()
        }
    }
}