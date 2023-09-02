package com.digital_tent.cow_marks.retrofit

import android.content.ContentValues.TAG
import android.content.Context
import android.util.Log
import android.widget.Toast
import com.digital_tent.cow_marks.db.Product
import com.digital_tent.cow_marks.db.ProductDB
import com.digital_tent.cow_marks.db.Workshop
import com.digital_tent.cow_marks.db.WorkshopDB
import com.google.gson.JsonParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import retrofit2.Retrofit

class WorkshopsJson(private val context: Context) {
    private val workshopDB = WorkshopDB.getDB(context).workshopDao()
    private val productDB = ProductDB.getDB(context).productDao()

    private val api: WorkshopsJsonAPI = Retrofit.Builder()
        .baseUrl("http://172.16.16.239")
        .build()
        .create(WorkshopsJsonAPI::class.java)

    fun connect() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Сброс данных в базе для обновления
                workshopDB.deleteBase()
                productDB.deleteBase()

                // Погружаемся в json для сохранения в базу
                val response = api.getWorkshops()
                val jsonString = response.body()?.string()
                val json = JsonParser.parseString(jsonString).asJsonObject
                val workshops = json.keySet()

                for (workshop in workshops) {
                    val lineObject = json[workshop].asJsonObject["Line"].asJsonObject
                    val lines = lineObject.keySet()
                    for (line in lines) {
//                        Log.e(TAG, "connect: $line")
                        val productObject = lineObject[line].asJsonObject["Products"].asJsonObject
                        val products = productObject.keySet()
                        for (product in products) {
                            val name =
                                productObject.asJsonObject[product].asJsonObject["name"].asString
                            val gtin =
                                productObject.asJsonObject[product].asJsonObject["gtin"].asString
                            val life =
                                productObject.asJsonObject[product].asJsonObject["life"].asInt
                            val tnved =
                                productObject.asJsonObject[product].asJsonObject["tnved"].asString
                            val box =
                                productObject.asJsonObject[product].asJsonObject["box"].asInt
                            val pallet =
                                productObject.asJsonObject[product].asJsonObject["pallet"].asInt
//                            Log.e(TAG, "connect: $name\n$gtin\n$life\n$tnved\n$box\n$pallet")

                            // Запись данных в базу с цехами
                            workshopDB.addWorkshop(
                                Workshop(
                                    id = null,
                                    workshop = workshop,
                                    line = line,
                                    name = name,
                                    product = product
                                )
                            )
                            // Запись данных в базу с линиями
                            productDB.addProduct(
                                Product(
                                    id = null,
                                    name = name,
                                    article = product,
                                    workshop = workshop,
                                    line = line,
                                    gtin = gtin,
                                    life = life,
                                    tnved = tnved,
                                    box = box,
                                    pallet = pallet

                                )
                            )
                        }
                    }
                }
//                Log.e(TAG, "connect: ${workshopDB.getLineByWorkshop(workshopDB.getWorkshops().distinct().single()).distinct()}")
            } catch (exception: HttpException) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        context.applicationContext,
                        "Ошибка соединения",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
                Log.e(TAG, "Страница не найдена")
            }
        }
    }
}