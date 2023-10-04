package com.digital_tent.cow_marks.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ProductDao {

    @Insert
    suspend fun addProduct(product: Product)

    // Получить продукт по Gtin
    @Query("SELECT name FROM products WHERE gtin = :gtin")
    fun getProductByGtin(gtin: String): String

    // Получить Gtin по продукту
    @Query("SELECT gtin FROM products WHERE name = :product")
    fun getGtinByProduct(product: String): String

    // Получить срок годности продукта
    @Query("SELECT life FROM products WHERE name = :product")
    fun getLifeByProduct(product: String): Int

    // Проверить наличие продукта в базе
    @Query("SELECT EXISTS(SELECT 1 FROM products WHERE gtin = :gtin)")
    fun hasProduct(gtin: String): Boolean

    // Получить tnved_code продукта
    @Query("SELECT tnved FROM products WHERE name = :product")
    fun getTnVedCode(product: String): String

    // Получить список продуктов по линии
    @Query("SELECT name FROM products WHERE line = :line ")
    fun getProductsByLine(line: String): List<String>

    // Получить список gtin-ов по от продуктов по линии
    @Query("SELECT gtin FROM products WHERE line = :line")
    fun getGtinByLine(line: String): List<String>

    // Удалить все строки в базе данных
    @Query("DELETE FROM products")
    fun deleteBase()
}