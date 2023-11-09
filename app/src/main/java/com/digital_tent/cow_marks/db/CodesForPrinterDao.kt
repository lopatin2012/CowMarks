package com.digital_tent.cow_marks.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface CodesForPrinterDao {
    // Добавление кода в базу.
    @Insert
    suspend fun addCodesForPrinter(codesForPrinter: CodesForPrinter)

    // Получить коды из базы для печати.
    @Query("SELECT code FROM codes_for_printer WHERE code LIKE '%' || :gtin || '%' LIMIT 10")
    fun getCodesForPrinter(gtin: String): List<String>
    // Удаление кода из базы.
    @Query("DELETE FROM codes_for_printer WHERE code =:code")
    fun deleteCodeForPrinter(code: String)
    // Удаление устаревших кодов.
}