package com.digital_tent.cow_marks.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Product::class], version = 1, exportSchema = false)
abstract class ProductDB : RoomDatabase() {
    abstract fun productDao(): ProductDao

    companion object {
        @Volatile
        private var INSTANCE: ProductDB? = null
        fun getDB(context: Context): ProductDB {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }

            return Room.databaseBuilder(
                context.applicationContext,
                ProductDB::class.java,
                "product.db"
            ).build()
        }
    }
}