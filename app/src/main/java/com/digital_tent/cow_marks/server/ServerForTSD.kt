package com.digital_tent.print_and_scan

import android.content.ContentValues.TAG
import android.content.Context
import android.util.Log
import androidx.constraintlayout.helper.widget.MotionEffect
import com.digital_tent.cow_marks.GlobalVariables
import com.digital_tent.cow_marks.db.Code
import com.digital_tent.cow_marks.db.CodeDB
import com.digital_tent.cow_marks.db.ProductDB
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.ServerSocket
import java.text.DateFormat
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.log

class ServerForTSD(
    context: Context,
    globalVariables: GlobalVariables,
) :
    Thread() {
    private val context: Context
    private val globalVariables: GlobalVariables
    private val executor: ExecutorService
    private val codeDB: CodeDB = CodeDB.getDB(context)
    private val productDB: ProductDB = ProductDB.getDB(context)


    init {
        this.context = context
        this.globalVariables = globalVariables
        executor = Executors.newFixedThreadPool(8) // Количество потоков в пуле
    }

    override fun run() {
        try {
            ServerSocket(globalVariables.getTerminalPort()).use { serverSocket ->
                while (true) {
                    Log.e(MotionEffect.TAG, "Ожидание подключения")
                    val socket = serverSocket.accept()
                    Log.e(MotionEffect.TAG, "Подключение установлено")
                    Log.e(TAG, "run: ${socket.getInputStream().read()}", )
                    executor.execute {
                        try {
                            BufferedReader(InputStreamReader(socket.getInputStream())).use { `in` ->
                                BufferedWriter(OutputStreamWriter(socket.getOutputStream())).use { out ->
                                    var inputLine: String
                                    while (`in`.readLine()
                                            .also { inputLine = it } != null && inputLine != "exit"
                                    ) {
                                        // Данные по терминалу
                                        val terminal: String = globalVariables.getTerminalName()
                                        // Данные по продукту
                                        val job: String = globalVariables.getJobWork()
                                        val date: String = globalVariables.getDateWork()
                                        val party: String = globalVariables.getPartyWork()
                                        val productName: String = globalVariables.getProductWork()
                                        val code = inputLine.split(" ".toRegex())
                                            .dropLastWhile { it.isEmpty() }
                                            .toTypedArray()
                                        Log.e(TAG, "run: $inputLine")
                                        val codeDataMatrix = code[0]
                                        val gtin = code[0].substring(2, 16)
                                        Log.e(TAG, "coe $inputLine", )
                                        Log.e(MotionEffect.TAG, gtin)
                                        Log.e(MotionEffect.TAG, "Сервер получил код: $inputLine")
                                        if (inputLine.contains(globalVariables.getGtinWork())) {
                                            when (code[1]) {
                                                "0" -> {
                                                    val product_in_db: String =
                                                        productDB.productDao()
                                                            .getProductByGtin(gtin)
                                                    Log.e(TAG, "run: $gtin",)
                                                    val party_in_db: String =
                                                        codeDB.codeDao()
                                                            .getPartyByCode(codeDataMatrix)
                                                    val date_in_db: String =
                                                        codeDB.codeDao()
                                                            .getDateByCode(codeDataMatrix)
                                                    val job_in_db: String =
                                                        codeDB.codeDao()
                                                            .getJobByCode(codeDataMatrix)
                                                    if (codeDB.codeDao().hasCode(codeDataMatrix)) {
                                                        Log.e(
                                                            MotionEffect.TAG,
                                                            "Данные кода: " + code[0]
                                                        )
                                                        out.write(
                                                            "0;;;;;" + date_in_db + ";;;;;" + party_in_db +
                                                                    ";;;;;" + product_in_db + ";;;;;" + terminal +
                                                                    ";;;;;" + job_in_db
                                                        )
                                                    } else {
                                                        Log.e(
                                                            MotionEffect.TAG,
                                                            "Код не добавлен"
                                                        )
                                                        out.write(
                                                            "1;;;;;Отсутствует;;;;;Отсутствует;;;;;" + product_in_db
                                                                    + ";;;;;" + terminal +
                                                                    ";;;;;Отсутствует"
                                                        )
                                                    }
                                                    out.newLine() // Добавленная строка
                                                    out.write("exit")
                                                    out.newLine() // Добавлен
                                                    out.flush()
                                                }

                                                "1" -> {
                                                    val product_in_db: String =
                                                        productDB.productDao()
                                                            .getProductByGtin(gtin)
                                                    val party_in_db: String =
                                                        codeDB.codeDao()
                                                            .getPartyByCode(codeDataMatrix)
                                                    val date_in_db: String =
                                                        codeDB.codeDao()
                                                            .getDateByCode(codeDataMatrix)
                                                    val job_in_db: String =
                                                        codeDB.codeDao()
                                                            .getJobByCode(codeDataMatrix)
                                                    Log.e(MotionEffect.TAG, "Режим добавления")
                                                    Log.e(MotionEffect.TAG, codeDataMatrix)
                                                    if (codeDB.codeDao().hasCode(codeDataMatrix)) {
                                                        Log.e(
                                                            MotionEffect.TAG,
                                                            "Код уже в базе: " + code[0]
                                                        )
                                                        out.write(
                                                            "1;;;;;" + date_in_db + ";;;;;" + party_in_db +
                                                                    ";;;;;" + product_in_db + ";;;;;" + terminal +
                                                                    ";;;;;" + job_in_db
                                                        )
                                                    } else {
                                                        Log.e(
                                                            MotionEffect.TAG,
                                                            "Код добавлен " + codeDataMatrix + " в базу"
                                                        )
                                                        out.write(
                                                            "0;;;;;" + date + ";;;;;" + party +
                                                                    ";;;;;" + product_in_db + ";;;;;" + terminal +
                                                                    ";;;;;" + job
                                                        )
                                                        codeDB.codeDao().addCode(
                                                            Code(
                                                                null,
                                                                code = codeDataMatrix,
                                                                date = date,
                                                                party = party,
                                                                job = job,
                                                                time = System.currentTimeMillis(),
                                                                printer = 0,
                                                                valid = 1
                                                            )
                                                        )


                                                    }
                                                    out.newLine() // Добавленная строка
                                                    out.write("exit")
                                                    out.newLine() // Добавленная строка
                                                    out.flush()
                                                    out.write("exit")
                                                }

                                                "2" -> {
                                                    val product_in_db: String =
                                                        productDB.productDao()
                                                            .getProductByGtin(gtin)
                                                    val party_in_db: String =
                                                        codeDB.codeDao()
                                                            .getPartyByCode(codeDataMatrix)
                                                    val date_in_db: String =
                                                        codeDB.codeDao()
                                                            .getDateByCode(codeDataMatrix)
                                                    val job_in_db: String =
                                                        codeDB.codeDao()
                                                            .getJobByCode(codeDataMatrix)
                                                    if (codeDB.codeDao().hasCode(codeDataMatrix)) {
                                                        Log.e(
                                                            MotionEffect.TAG,
                                                            "Код удалён"
                                                        )
                                                        // Удаление кода
                                                        codeDB.codeDao()
                                                            .deleteCodeTSD(codeDataMatrix)
                                                        out.write(
                                                            "1;;;;;" + date_in_db + ";;;;;" + party_in_db +
                                                                    ";;;;;" + product_in_db + ";;;;;" + terminal +
                                                                    ";;;;;" + job_in_db
                                                        )
                                                    } else {
                                                        Log.e(
                                                            MotionEffect.TAG,
                                                            "Код в базе отсутствует"
                                                        )
                                                        out.write(
                                                            "0;;;;;" + date + ";;;;;" + party +
                                                                    ";;;;;" + product_in_db + ";;;;;" + terminal +
                                                                    ";;;;;" + job
                                                        )
                                                    }
                                                    out.newLine() // Добавленная строка
                                                    out.write("exit")
                                                    out.newLine() // Добавлен
                                                    out.flush()
                                                }
                                            }
                                        }  else {
                                            val product_in_db: String =
                                                productDB.productDao().getProductByGtin(gtin)
                                            val party_in_db: String =
                                                codeDB.codeDao()
                                                    .getPartyByCode(codeDataMatrix)
                                            val date_in_db: String =
                                                codeDB.codeDao()
                                                    .getDateByCode(codeDataMatrix)
                                            val job_in_db: String =
                                                codeDB.codeDao()
                                                    .getJobByCode(codeDataMatrix)
                                            out.write(
                                                "2;;;;;Отсутствует;;;;;Отсутствует;;;;;" + product_in_db
                                                        + ";;;;;" + terminal +
                                                        ";;;;;Отсутствует"
                                            )
                                            out.newLine() // Добавленная строка
                                            out.write("exit")
                                            out.newLine() // Добавленная строка
                                            out.flush()
                                        }
                                    }
                                    Log.e(
                                        MotionEffect.TAG,
                                        "Соединение закрыто"
                                    )
                                }
                            }
                        } catch (e: IOException) {
                            Log.e(
                                MotionEffect.TAG,
                                "Ошибка взаимодействия с клиентом",
                                e
                            )
                        }
                    }
                }
            }
        } catch (e: IOException) {
            Log.e(MotionEffect.TAG, "Были ошибки при работе сервера", e)
        }
    }
}