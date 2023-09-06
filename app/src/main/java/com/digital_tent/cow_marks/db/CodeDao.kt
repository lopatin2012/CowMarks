package com.digital_tent.cow_marks.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface CodeDao {

    @Insert
    fun addCode(code: Code)

    // Проверить наличие кода в базе
    @Query("SELECT EXISTS(SELECT 1 FROM codes WHERE code = :code AND valid = 1)")
    fun hasCode(code: String): Boolean

    // Проверить количество дублей кода в базе
    @Query("SELECT COUNT(*) >= 2 FROM codes WHERE code = :code AND job =:job AND valid = 1")
    fun duplicates(code: String, job: String): Boolean

    // Удалить полученный код от Сервера
    @Query("DELETE FROM codes WHERE code = :code AND party =:party AND job =:job")
    fun deleteCodeServer(code: String, party: String, job: String)

    // Удаление кода
    @Query("DELETE FROM codes WHERE code = :code")
    fun deleteCodeTSD(code: String)
    //----------------------------------------------------------------
    // Для ТСД
    //----------------------------------------------------------------

    // Получить партию по коду
    @Query("SELECT party FROM codes WHERE code = :code")
    fun getPartyByCode(code: String): String

    // Получить Job(уникальный номер на линии) по коду
    @Query("SELECT job FROM codes WHERE code = :code")
    fun getJobByCode(code: String): String

    // Получить дату по коду
    @Query("SELECT date FROM codes WHERE code = :code")
    fun getDateByCode(code: String): String

    //----------------------------------------------------------------
    // Для формирование отчётов
    //----------------------------------------------------------------
    // Нахождение кодов по Gtin and Job and Party
    @Query("SELECT code FROM codes WHERE code LIKE '%' || :gtin || '%' AND party = :party AND job = :job AND valid = 1")
    fun getCodes(gtin: String, job: String, party: String): List<String>
    //----------------------------------------------------------------
    // Для очистки базы данных
    //----------------------------------------------------------------
    // Удаление устаревших кодов
    @Query("DELETE FROM codes WHERE time < :expirationTimeMillis")
    fun deleteExpiredRows(expirationTimeMillis: Long)
}