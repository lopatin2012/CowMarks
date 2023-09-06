package com.digital_tent.cow_marks.printer

import android.app.Activity
import android.content.Context
import android.util.Log
import com.digital_tent.cow_marks.GlobalVariables
import com.digital_tent.cow_marks.databinding.FragmentFactoryBinding
import java.io.IOException
import java.net.Socket

class CodePrinting(val globalVariables: GlobalVariables, binding: FragmentFactoryBinding) {

    // Получение биндинга Фрагмента Производства
    val binding = binding
    fun getPrinterTemplates(): List<String> {
        val printerIp = globalVariables.getPrinterIp()
        val printerPort = globalVariables.getPrinterPort()
        var reportPrinter = ""
        val templatesList: MutableList<String> = mutableListOf()
        val buffer = ByteArray(4096) // Размер буфера (примерно 100 кодов за раз)
        try {
            Socket(printerIp, printerPort).use { socket ->
                // Отправка и получение команд
                val output = socket.getOutputStream()
                val input = socket.getInputStream()
                // Чистка синтаксиса передачи команд на принтер
                output.write("\r".toByteArray())
                output.flush()
                // Очистка буфера серилизации
                output.write("SCB\r".toByteArray())
                output.flush()
                // Получение ответа от принтера
                reportPrinter = input.read(buffer).toString()
                Log.e("Printer","Ответ принтера: $reportPrinter")
                // Команда получения списка шаблонов для печати
                output.write("GJL".toByteArray())
                output.flush()
                // Получение ответа принтера
                var reportList = input.read(buffer).toString().split('|')
                val reportListSize = reportList.size
                reportList = reportList.slice(2 until reportListSize)
                for (template in reportList) {
                    templatesList.add(template)
                }
                output.write("\r".toByteArray())
                output.flush()
            }
        } catch (e: IOException) {
            Log.e("Исключительный ответ принтера: ", e.toString(), )
        }
        return templatesList
    }

    // Выбор шаблона
    fun setJobSelect(template: String) {
        val printerIp = globalVariables.getPrinterIp()
        val printerPort = globalVariables.getPrinterPort()
        var reportPrinter = ""
        val buffer = ByteArray(4096) // Размер буфера (примерно 100 кодов за раз)
        try {
            Socket(printerIp, printerPort).use { socket ->
                // Отправка и получение команд
                val output = socket.getOutputStream()
                val input = socket.getInputStream()
                // Чистка синтаксиса передачи команд на принтер
                output.write("\r".toByteArray())
                output.flush()
                // Команда для смена шаблона
                output.write("SLA|$template|\r".toByteArray())
                output.flush()
                // Получение ответа от принтера
                reportPrinter = input.read(buffer).toString()
                Log.e("printer", "Ответ принтера: $reportPrinter")
            }
        } catch (e: IOException) {
            Log.e("Исключительный ответ принтера: ", e.toString(),)
        }
    }
}