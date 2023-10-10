package com.digital_tent.cow_marks.camera

import android.app.Activity
import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Context
import android.graphics.Color
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import com.digital_tent.cow_marks.GlobalVariables
import com.digital_tent.cow_marks.databinding.FragmentFactoryBinding
import com.digital_tent.cow_marks.db.Code
import com.digital_tent.cow_marks.db.CodeAll
import com.digital_tent.cow_marks.db.CodeAllDB
import com.digital_tent.cow_marks.db.CodeDB
import com.digital_tent.cow_marks.db.CodeDao
import com.digital_tent.cow_marks.db.ProductDB
import com.digital_tent.cow_marks.json.JsonAndDate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.lang.System.currentTimeMillis
import java.net.InetSocketAddress
import java.net.Socket


class OneCode(
    context: Context,
    activity: Activity,
    globalVariables: GlobalVariables,
    binding: FragmentFactoryBinding

) : Thread() {
    private var context: Context
    private var activity: Activity
    private var globalVariables: GlobalVariables

    init {
        this.context = context
        this.activity = activity
        this.globalVariables = globalVariables
    }

    private var textPlan: TextView =
        binding.factoryPlan // Изменение текста состояния кода
    private var textCounter: TextView = binding.factoryCounter // Изменение числа на счётчике
    private var textProduct: TextView = binding.factoryProduct // Изменения поля продукта
    private var frameColor: FrameLayout = binding.factoryFrameCounter// Изменение цвета счётчика
    private var textNotification: TextView = binding.factoryNotification // Уведомление

    // Переменные для взаимодействия c входящими данными
    private var evenScan = true // Чётность сканирования
    private var factoryCounter = 0 // Счётчик
    private var byteReader = 0 // Двоичное представление прочитанного кода
    private var buffer = ByteArray(2048) // Размер буфера (примерно 55 кодов за раз)

    // Переменные для отправки периодических отчётов без кодов
    // Путь к файлу
    private lateinit var pathFile: String
    private lateinit var workshop: String
    private lateinit var line: String
    private lateinit var plan: String
    private val status = "В работе"

    // Массив с кодами.
    private val arrayOfCodes: ArrayList<String> = arrayListOf()

    // Счётчик для отправки отчёта на сервер
    private var sendCounter = 0

    override fun run() {
        // База данных
        val codeDB = CodeDB.getDB(context).codeDao()
        val productDB = ProductDB.getDB(context).productDao()
        // Получение переменных для работы сканирования
        // Глобальные переменные
        // Дата
        val date: String = globalVariables.getDateWork()
        // Партия
        val party: String = globalVariables.getPartyWork()
        // Gtin
        val gtin: String = globalVariables.getGtinWork()
        // Задание
        val job: String = globalVariables.getJobWork()
        // Цех.
        workshop = globalVariables.getWorkshop()
        // Линия.
        line = globalVariables.getLine()
        // План производства.
        plan = globalVariables.getPlanWork()

        // Регулярное выражение
        val jsonAndDate = JsonAndDate(context)

        // Актуализация счётчика
        globalVariables.setCounter(codeDB.getCodes(gtin, job, party).distinct().size.toString())
        factoryCounter = globalVariables.getCounter().toInt()
        activity.runOnUiThread {
            textCounter.text = factoryCounter.toString()
        }
        // добавление кодов в массив из базы.
        CoroutineScope(Dispatchers.IO).launch {
            for (i in codeDB.getCodes(gtin, job, party)) {
                arrayOfCodes.add(i)
            }
        }
//        Log.e(TAG, "party: $party gtin: $gtin jobwork: $jobWork Количество кодов: $factoryCounter", )


        // Подключение к камере
        val ipCamera = globalVariables.getCameraIp()
        val portCamera = globalVariables.getCameraPort()
        try {
            Socket(ipCamera, portCamera).use { socket ->
                // Изменение цвета продукта на зелёный.
                activity.runOnUiThread {
                    textProduct.setBackgroundColor(
                        Color.GREEN
                    )
                }
                while (socket.getInputStream().read(buffer).also { byteReader = it } != -1) {
                    // Остановка сканирования.
                    if (!globalVariables.getScanning()) {
                        activity.runOnUiThread {
                            textProduct.setBackgroundColor(Color.RED)
                        }
                        return
                    }
                    if (currentThread().isInterrupted) {
//                            Log.d(TAG, "run: Поток был прерван")
                        return
                    }
                    val inputString = String(buffer, 0, byteReader)
                    val codesDataMatrix = jsonAndDate.extractCameraData(inputString)
                    CoroutineScope(Dispatchers.IO).launch {
                        for (code in codesDataMatrix) {
                            // Проверка режима работы для получения кодов с одной/связки камер на терминале.
                            // Если код не соответствует заданию, то красный экран
                            if (!code.contains(gtin)) {
                                val gtinProduct = code.substring(2, 16)
                                val product = productDB.getProductByGtin(gtinProduct)
                                activity.runOnUiThread {
                                    val text =
                                        "Статус кода: не соответствует заданию.\n Продукт: $product"
                                    textNotification.text = text
                                    frameColor.setBackgroundColor(Color.RED)
                                }
                                // Если код уже есть в базе, то голубой экран
                            } else if (arrayOfCodes.contains(code)) {
                                activity.runOnUiThread {
                                    textNotification.text =
                                        "Статус кода: есть в базе или обнаружен дубль"
                                    frameColor.setBackgroundColor(Color.CYAN)
                                }
                            } else {
                                codeDB.addCode(
                                    Code(
                                        id = null,
                                        code = code,
                                        date = date,
                                        party = party,
                                        job = job,
                                        time = currentTimeMillis(),
                                        printer = 0,
                                        valid = 1
                                    )
                                )
                                // Добавление кода в массив.
                                arrayOfCodes.add(code)
                                //                        Log.d(TAG, "run: Код добавлен")
                                factoryCounter = globalVariables.getCounter().toInt()
                                factoryCounter += 1
                                globalVariables.setCounter(factoryCounter.toString())
                                factoryCounter = globalVariables.getCounter().toInt()
                                if (evenScan) {
                                    activity.runOnUiThread {
                                        textNotification.text =
                                            "Статус кода: добавлен в базу"
                                        textCounter.text = factoryCounter.toString()
                                        frameColor.setBackgroundColor(Color.GREEN)
                                    }
                                    //                            Log.d(TAG, "run: Зелёный")
                                } else {
                                    activity.runOnUiThread {
                                        textNotification.text =
                                            "Статус кода: добавлен в базу"
                                        textCounter.text = factoryCounter.toString()
                                        frameColor.setBackgroundColor(Color.YELLOW)
                                    }
                                    //                            Log.d(TAG, "run: Жёлтый")
                                }
                                evenScan = !evenScan
                            }
                            //                            Log.e(ContentValues.TAG, "inputCode: $inputString")
                            //                            Log.e(ContentValues.TAG, "code: $code")
                            globalVariables.setCounter(factoryCounter.toString())
                        }
                    }
                    // Если счётчик равен 100, то скинуть файл на сервер
                    if (sendCounter == 100) {
                        report(gtin, date, party, job, codeDB, jsonAndDate)
                        sendCounter = 0
                    } else {
                        sendCounter += 1
                    }
                }
            }
            // Не удалось подключиться к камере
        } catch (e: IOException) {
            activity.runOnUiThread {
                Toast.makeText(
                    activity.applicationContext,
                    "Ошибка: не удалось подключиться к камере",
                    Toast.LENGTH_SHORT
                ).show()
                textProduct.setBackgroundColor(
                    Color.RED
                )
            }
        }
    }

    // Создание отчёта каждые n-е количество тиков сканирования
    private fun report(
        gtin: String,
        date: String,
        party: String,
        job: String,
        codeDB: CodeDao,
        jsonAndDate: JsonAndDate
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            pathFile =
                "${workshop}_${line}_${gtin}_${factoryCounter}_${date}_" +
                        "${party}_${job}_${plan}_${status}.json"
            File(pathFile)
//            val listCodes = codeDB.getCodes(gtin, job, party).distinct()
            val listCodes = arrayOfCodes.toList()
            val jsonFile = jsonAndDate.createJsonFile(
                context = context,
                expDate = globalVariables.getExpDateWork(),
                production_date = globalVariables.getDateWork(),
                tnved_code = globalVariables.getTNvedCode(),
                party = party,
                gtin = gtin,
                listCodes = listCodes,
                job = job,
                nameTerminal = line,
                workshopFile = workshop,
                plan = plan,
                status = status
            )
            jsonAndDate.uploadFileToServer(jsonFile)
//                                Log.e(TAG, "Отчёт: Файл создан", )
        }
    }
}