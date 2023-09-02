package com.digital_tent.cow_marks.db

import androidx.room.Dao
import androidx.room.Insert

@Dao
interface CodeAllDao {

    @Insert
    fun addCode(codeAll: CodeAll)
}