package com.digital_tent.cow_marks.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface CodeAllDao {

    @Insert
    fun addCode(codeAll: CodeAll)

    // Проверить наличие кода в базе
    @Query("SELECT EXISTS(SELECT 1 FROM codes_all WHERE code = :code AND valid = 1)")
    fun hasCode(code: String): Boolean

    // Удалить код
    @Query("DELETE FROM codes_all WHERE code = :code")
    fun deleteCode(code: String)

    // Нахождение кодов по Gtin and Job and Party
    @Query("SELECT code FROM codes_all WHERE code LIKE '%' || :gtin || '%' AND party = :party AND job = :job AND valid = 1")
    fun getCodes(gtin: String, job: String, party: String): List<String>

}