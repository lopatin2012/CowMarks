package com.digital_tent.cow_marks.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity (tableName = "workshops")
data class Workshop (
    @PrimaryKey (autoGenerate = true)
    val id: Int? = null,
    val workshop: String,
    val line: String,
    val name: String,
    val product: String
)