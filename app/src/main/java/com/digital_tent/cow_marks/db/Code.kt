package com.digital_tent.cow_marks.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "codes")
data class Code(
    @PrimaryKey (autoGenerate = true)
    val id: Int? = null,
    val code: String,
    val date: String,
    val party: String,
    val job: String,
    val time: Long,
    val printer: Int,
    val valid: Int
)