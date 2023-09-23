package com.digital_tent.cow_marks.printer

import android.content.ContentValues
import android.util.Log
import com.digital_tent.cow_marks.GlobalVariables
import com.digital_tent.cow_marks.databinding.FragmentFactoryBinding
import java.net.Socket

class CodePrinting(val globalVariables: GlobalVariables, binding: FragmentFactoryBinding) {
    // ACK - положительный ответ принтера

    private lateinit var stringTemplates: String

    // Очистка очереди печати
    fun clear() {
        val printerIP = globalVariables.getPrinterIp()
        val printerPORT = globalVariables.getPrinterPort()
        val buffer = ByteArray(1024) // Размер буфера
        try {
                Socket(printerIP, printerPORT).use {
                    val output = it.getOutputStream()
                    val input = it.getInputStream()
                    // Чистка синтаксиса передачи команд на принтер
                    output.write("\r".toByteArray())
                    output.flush()
                    // Очистка буфера серилизации
                    output.write("SCB\r".toByteArray())
                    output.flush()
                    Log.e("SCB: ", String(buffer, 0, input.read(buffer)))
                    // Чистка синтаксиса передачи команд на принтер
                    output.write("\r".toByteArray())
                    output.flush()

                }
        } catch (e: Exception) {
            Log.e(ContentValues.TAG, "Ошибка при отправке команды: ${e.message}")
        }
    }

    // Получить список шаблонов
    fun getTemplatesList(): String {
        val printerIP = globalVariables.getPrinterIp()
        val printerPORT = globalVariables.getPrinterPort()
        val buffer = ByteArray(1024) // Размер буфера
        try {
            Socket(printerIP, printerPORT).use {
                val output = it.getOutputStream()
                val input = it.getInputStream()
                // Чистка синтаксиса передачи команд на принтер
                output.write("\r".toByteArray())
                output.flush()
                // Команда получения списка шаблонов для печати
                output.write("GJL\r".toByteArray())
                output.flush()
                stringTemplates = String(buffer, 0, input.read(buffer))
                Log.e("GJL: ", stringTemplates)
                // Чистка синтаксиса передачи команд на принтер
                output.write("\r".toByteArray())
                output.flush()
            }
        } catch (e: Exception) {
            Log.e(ContentValues.TAG, "Ошибка при отправке команды: ${e.message}")
        }
        return stringTemplates
    }

    // Получить список шаблонов
    // Первые 2 элемента игнорируются, как и 2 последних
    // Пример приходящих данных: JBL|4|TimeOFF Top DM C-off|TimeOFF Top DM|Top DM C-off|Top DM|
    fun listTemplates(): List<String> {
        val mutableListTemplates = stringTemplates.split("|").toMutableList()
        return mutableListTemplates.slice(2 until mutableListTemplates.size - 1)
    }

    // Печать кодов
    fun printingCodes(template: String, productDate: String, expirationDate: String,
                      code: String, party: String) {
        val printerIP = globalVariables.getPrinterIp()
        val printerPORT = globalVariables.getPrinterPort()
        val buffer = ByteArray(1024) // Размер буфера
        try {
            Socket(printerIP, printerPORT).use {
                val output = it.getOutputStream()
                val input = it.getInputStream()
                // Чистка синтаксиса передачи команд на принтер
                output.write("\r".toByteArray())
                output.flush()
                // Выбор шаблона для печати
                output.write("SLA|$template|\r".toByteArray())
                output.flush()
                Log.e("SLA: ", String(buffer, 0, input.read(buffer)))
                // Получение сведений о свободном месте
                output.write("SFS\r".toByteArray())
                output.flush()
                Log.e("SFS: ", String(buffer, 0, input.read(buffer)))
                // Получение сведений о максимальном количестве записей в режиме серилизации
                output.write("SGM\r".toByteArray())
                output.flush()
                Log.e("SGM: ", String(buffer, 0, input.read(buffer)))
                // Получение количества записей в режиме серилизации
                output.write("SRC\r".toByteArray())
                output.flush()
                Log.e("SRC: ", String(buffer, 0, input.read(buffer)))
                // Подготовка переменных для отправки на печать
                val gtin = code.substring(2, 17)
                val sn = code.substring(18, 25)
                val kripto = code.substring(27)
                // Отправка на печать
                output.write(("SHD|PRODUCT_DATE=$productDate|EXPIRATION_DATE=$expirationDate|" +
                        "GTIN=$gtin|SN=$sn|KRIPTO=$kripto|PARTY=$party|\r").toByteArray())
                output.flush()
                Log.e("SHD: ", String(buffer, 0, input.read(buffer)))
                // Чистка синтаксиса передачи команд на принтер
                output.write("\r".toByteArray())
                output.flush()
            }
        } catch (e: Exception) {
            Log.e(ContentValues.TAG, "Ошибка при отправке команды: ${e.message}")
        }
    }
}