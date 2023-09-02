package com.digital_tent.cow_marks.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity (tableName = "products")
data class Product(
    @PrimaryKey (autoGenerate = true)
    val id: Int? = null,
    val name: String,
    val article: String,
    val workshop: String,
    val line: String,
    val gtin: String,
    val life: Int,
    val tnved: String,
    val box: Int,
    val pallet: Int,

)