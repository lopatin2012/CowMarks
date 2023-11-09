package com.digital_tent.cow_marks.printer

import android.content.ContentValues
import android.content.Context
import android.util.Log
import com.digital_tent.cow_marks.GlobalVariables
import com.digital_tent.cow_marks.databinding.FragmentFactoryBinding
import com.digital_tent.cow_marks.db.CodesForPrinter
import com.digital_tent.cow_marks.db.CodesForPrinterDB
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.Socket
import kotlin.concurrent.timer

class CodePrinting(val globalVariables: GlobalVariables, contextForPrinter: Context) {

    // Общая информация о командах принтера.

    // Получение сведений о максимальном количестве записей в режиме серилизации
    //    output.write("SGM\r".toByteArray())
    //    output.flush()
    //    Log.e("SGM: ", String(buffer, 0, input.read(buffer)))
    //
    // ACK - положительный ответ принтера

    // База данных.
    private val codesForPrinterDB = CodesForPrinterDB.getDB(context = contextForPrinter)
        .CodesForPrinterDao()

    private var stringTemplates: String = "Пустой шаблон"

    // Параметры для работы с принтером по сети
    private val printerIP = globalVariables.getPrinterIp()
    private val printerPORT = globalVariables.getPrinterPort()
    private var buffer = ByteArray(1024) // Размер буфера
    private var src: Int = 0
    private var pause: Boolean = false

    // Очистка очереди печати
    fun clear() {
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
        try {
            Socket(printerIP, printerPORT).use {
                val output = it.getOutputStream()
                val input = it.getInputStream()
//                // Чистка синтаксиса передачи команд на принтер
//                output.write("\r".toByteArray())
//                output.flush()
                // Команда получения списка шаблонов для печати
                output.write("GJL\r".toByteArray())
                output.flush()
                stringTemplates = String(buffer, 0, input.read(buffer))
                Log.e("GJL: ", stringTemplates)
//                // Чистка синтаксиса передачи команд на принтер
//                output.write("\r".toByteArray())
//                output.flush()
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

    fun setTemplate(template: String) {
        Log.e("Шаблон для печати: ", globalVariables.getTemplate())
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
                // Чистка синтаксиса передачи команд на принтер
                output.write("\r".toByteArray())
                output.flush()
            }
        } catch (e: Exception) {
            Log.e(ContentValues.TAG, "Ошибка при отправке команды: ${e.message}")
        }
    }


    // Печать кодов.
    suspend fun printingCodes(
        productDate: String, expirationDate: String,
        party: String
    ) {
        try {
            // Установить статус печати.
            globalVariables.setPrinting(true)
            withContext(Dispatchers.IO) {
                Socket(printerIP, printerPORT).use {
                    val output = it.getOutputStream()
                    val input = it.getInputStream()
                    val template = globalVariables.getTemplate()
                    // Чистка синтаксиса передачи команд на принтер
                    output.write("\r".toByteArray())
                    output.flush()
                    // Очистка буфера серилизации
                    output.write("SCB\r".toByteArray())
                    output.flush()
                    Log.e("SCB: ", String(buffer, 0, input.read(buffer)))
//                     Выбор шаблона для печати
                    output.write("SEL|$template|\r".toByteArray())
                    output.flush()
                    Log.e("SEL: ", String(buffer, 0, input.read(buffer)))
                    // Установить максимальное количество на печать.
                    output.write("SMR|250|\r".toByteArray())
                    output.flush()
                    Log.e("SMR: ", String(buffer, 0, input.read(buffer)))
                    while (globalVariables.getPrinting()) {
                        delay(300)
                        // Получение сведений о свободном месте
                        output.write("SRC\r".toByteArray())
                        output.flush()
                        src = String(buffer, 0, input.read(buffer))
                            .split("|")[1]
                            .toInt()
                        Log.e("SRC", "printingCodes: $src")
                        if (src < 5) {
                            val listCodes =
                                codesForPrinterDB.getCodesForPrinter(globalVariables.getGtinWork())
                                    .toMutableList()
                            Log.e("Коды в списке", listCodes.toString())
                            for (code in listCodes) {
                                delay(50)
                                CoroutineScope(Dispatchers.IO).launch {
                                    codesForPrinterDB.deleteCodeForPrinter(code)
                                }
                                listCodes.remove(code)
                                // Подготовка переменных для отправки на печать
                                val gtin = code.substring(2, 17)
                                val sn = code.substring(18, 25)
                                val kripto = code.substring(27)
                                // Отправка на печать
                                output.write(
                                    ("SHD|PRODUCT_DATE=$productDate|EXPIRATION_DATE=$expirationDate|" +
                                            "GTIN=$gtin|SN=$sn|KRIPTO=$kripto|PARTY=$party|\r")
                                        .toByteArray()
                                )
                                output.flush()
                                Log.e("Код отправлен: ", String(buffer, 0, input.read(buffer)))
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(ContentValues.TAG, "Ошибка при отправке команды: ${e.message}")
        }
    }
}