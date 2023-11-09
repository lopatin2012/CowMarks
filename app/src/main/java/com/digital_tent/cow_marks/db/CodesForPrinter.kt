package com.digital_tent.cow_marks.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity (tableName = "codes_for_printer")
data class CodesForPrinter (
    @PrimaryKey (autoGenerate = true)
    val id: Int? = null,
    val code: String,
    val date: String,
)