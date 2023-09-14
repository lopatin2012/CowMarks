package com.digital_tent.cow_marks.json

import android.annotation.SuppressLint
import android.content.Context
import com.google.gson.Gson
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Date
import java.util.Locale

class JsonAndDate(val context: Context) {

    // Чтение json-файла
    fun readJsonFile(filePath: String?): String {
        val file = filePath?.let { File(it) }
        if (file != null) {
            if (file.exists()) {
                val stringBuilder = StringBuilder()
                try {
                    val bufferedReader = BufferedReader(FileReader(filePath))
                    val gson = Gson()
                    val jsonObject = gson.fromJson(bufferedReader, Any::class.java)
                    stringBuilder.append(gson.toJson(jsonObject))
                } catch (e: IOException) {
                    throw RuntimeException(e)
                }
                return stringBuilder.toString()
            }
        }
        return "Пустая строка"
    }

    // Переименовывание файла
    @SuppressLint("SuspiciousIndentation")
    fun renameReport(name: String): String {

        val fileJob = File(context.filesDir, name)
        val edit = name.replace("Завершено", "Выгружено")
        val fileEdit = File(context.filesDir, edit)
        return try {
            if (fileJob.renameTo(fileEdit)) {
                edit
            } else {
                // Ошибка при переименовании файла
                name
            }
        } catch (e: Exception) {
            // Обработка исключения, если возникает ошибка в процессе переименования файла
            e.printStackTrace()
            name
        }
    }

    // Переименовывание файла на удаление
    fun renameReportDelete(name: String): String {

        val fileJob = File(context.filesDir, name)
        val edit = name.replace("Новое", "Удалено")
        val fileEdit = File(context.filesDir, edit)
        return try {
            if (fileJob.renameTo(fileEdit)) {
                edit
            } else {
                // Ошибка при переименовании файла
                name
            }
        } catch (e: Exception) {
            // Обработка исключения, если возникает ошибка в процессе переименования файла
            e.printStackTrace()
            name
        }
    }

    // турбо-чтение файла json в stream
    fun readJsonFileInputStream(inputStream: InputStream?): String {
        val stringBuilder = StringBuilder()
        try {
            val bufferedReader = BufferedReader(InputStreamReader(inputStream))
            var line: String?
            while (bufferedReader.readLine().also { line = it } != null) {
                stringBuilder.append(line)
            }
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
        return stringBuilder.toString()
    }

    // турбо-чтение текстового файла в stream
    fun readTxtFileInputStream(inputStream: InputStream?): List<String> {
        val codes: MutableList<String> = ArrayList()
        try {
            val bufferedReader = BufferedReader(InputStreamReader(inputStream))
            var line: String
            while (bufferedReader.readLine().also { line = it } != null) {
                // Добавляем строку сразу в список, пропуская пустые
                if (!line.trim { it <= ' ' }.isEmpty()) {
                    codes.add(line)
                }
            }
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
        return codes
    }

    // Преобразование текстового файла в список
    @Throws(IOException::class)
    fun readFileToList(file: File?): List<String> {
        val lines: MutableList<String> = ArrayList()
        BufferedReader(FileReader(file)).use { reader ->
            var line: String
            while (reader.readLine().also { line = it } != null) {
                lines.add(line)
            }
        }
        return lines
    }

    // Быстрый способ получить путь к файлу из папки приложения files
    private fun getFilePath(context: Context, fileName: String, fileExtension: String): String {
        val filesDir = context.filesDir
        val separator = File.separator
        return filesDir.absolutePath + separator + fileName + "." + fileExtension
    }

    fun createJsonFile(
        context: Context,
        expDate: String, production_date: String?, tnved_code: String?, party: String, gtin: String,
        listCodes: List<String?>, job: String, nameTerminal: String, workshopFile: String,
        plan: String, status: String
    ): String {
            val jsonObject = JSONObject()
            jsonObject.put("usageType", "")
            jsonObject.put("expDate", formatExpDate(expDate))
            jsonObject.put("expDate72", formatExpDate(expDate) + "1800")
            jsonObject.put("production_date", formatProductDate(production_date))
            jsonObject.put("tnved_code", tnved_code)
            jsonObject.put("partion", party)
            jsonObject.put("creationdate", formatToday(System.currentTimeMillis()))
            jsonObject.put("gtin", gtin.substring(1))
            val sntinsArray = JSONArray(listCodes)
            jsonObject.put("sntins", sntinsArray)
            val filename =
                "${workshopFile}_${nameTerminal}_${gtin}_${listCodes.size}_${party}_${formatDate(production_date)}_${job}_${plan}_${status}.json"
            val jsonString = jsonObject.toString()
            context.openFileOutput(filename, Context.MODE_PRIVATE).use { fos ->
                fos.write(
                    jsonString.toByteArray()
                )
            }
        return filename
    }

    private fun formatToday(toDay: Long): String? {
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return simpleDateFormat.format(toDay)
    }


    private fun formatExpDate(timeMillis: Long): String {
        val dateFormat = SimpleDateFormat("yyMMdd", Locale.getDefault())
        val date = Date(timeMillis)
        return dateFormat.format(date)
    }

    private fun formatProductDate(inputDate: String?): String? {
        val inputDateFormat = SimpleDateFormat("dd.MM.yy", Locale.getDefault())
        val outputDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return try {
            val date = inputDate?.let { inputDateFormat.parse(it) }!!
            outputDateFormat.format(date)
        } catch (e: ParseException) {
            e.printStackTrace()
            "Дата введена некорректно. Ошибка: $e"
        }
    }

    fun formatDate(inputDate: String?): String {
        val inputDateFormat = SimpleDateFormat("dd.MM.yy", Locale.getDefault())
        val outputDateFormat = SimpleDateFormat("ddMMyy", Locale.getDefault())
        return try {
            val date = inputDate?.let { inputDateFormat.parse(it) }!!
            outputDateFormat.format(date)
        } catch (e: ParseException) {
            e.printStackTrace()
            "Дата введена некорректно. Ошибка: $e"
        }
    }

    fun formatExpDate(inputDate: String?): String {
        val inputDateFormat = SimpleDateFormat("dd.MM.yy", Locale.getDefault())
        val outputDateFormat = SimpleDateFormat("yyMMdd", Locale.getDefault())
        return try {
            val date = inputDate?.let { inputDateFormat.parse(it) }!!
            outputDateFormat.format(date)
        } catch (e: ParseException) {
            e.printStackTrace()
            "Дата введена некорректно. Ошибка: $e"
        }
    }

    fun formatToUnix(inputDate: String?): Long {
        val dateFormat = "dd.MM.yy"
        val sdf = SimpleDateFormat(dateFormat, Locale.getDefault())
        return try {
            val date = inputDate?.let { sdf.parse(it) }!!
            date.time
        } catch (e: ParseException) {
            throw RuntimeException(e)
        }
    }

    // Добавить срок годности к дате производстве/маркировки
    fun addExpirationDays(date: String?, expirationDays: Int): String? {
        val formatter = DateTimeFormatter.ofPattern("dd.MM.yy")
        return try {
            val originalDate = LocalDate.parse(date, formatter)
            val newDate = originalDate.plusDays(expirationDays.toLong())
            formatter.format(newDate)
        } catch (e: DateTimeParseException) {
            // Возвращаем стандартный ответ, если возникает исключение
            System.err.println("Error parsing date: " + e.message)
            "Ошибка в дате"
        }
    }

    // Выгрузка отчёта на сервер
    fun uploadFileToServer(editFile: String) {
        val fileJson = File(context.filesDir, editFile)

        // Создайте экземпляр MediaType для указания типа содержимого
        val mediaType: MediaType? = MediaType.parse("application/json; charset=utf-8")

        // Создайте экземпляр RequestBody, используя метод create с MediaType и объектом File
        val requestBody: RequestBody? = RequestBody.create(mediaType, fileJson)

        // Проверьте, что RequestBody не равен null перед использованием
        if (requestBody != null) {
            val client = OkHttpClient()

            // Создайте экземпляр MultipartBody.Builder
            val builder = MultipartBody.Builder().setType(MultipartBody.FORM)

            // Добавьте файл в форму с именем "json_files"
            builder.addFormDataPart("json_files", fileJson.name, requestBody)

            // Получите окончательный RequestBody из MultipartBody.Builder
            val finalRequestBody: RequestBody = builder.build()

            // Создайте запрос с использованием окончательного RequestBody
            val request = Request.Builder()
                .url("http://172.16.16.239/upload_json")
                .post(finalRequestBody)
                .build()

            // Отправьте асинхронный запрос с использованием OkHttp
            client.newCall(request).enqueue(object : okhttp3.Callback {
                override fun onFailure(call: okhttp3.Call, e: IOException) {
                    e.printStackTrace()
                    // Обработка ошибки во время отправки
                    println("Неудача во время отправки")
                }

                override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                    response.use {
                        if (response.isSuccessful) {
                            // Успешный ответ
                            val responseBody = response.body()?.string()
                            println(responseBody)
                            println("Успех")
                        } else {
                            // Ошибка
                            val errorMessage = response.message()
                            println(errorMessage)
                            println("Ошибка")
                        }
                    }
                }
            })
        }
    }

    // Чистка файлов. Если более 7 дней, то удалить
    fun fileCheckerAndDelete() {
        val folderPath = context.applicationContext.filesDir.toString()
        val folder = File(folderPath)
        val files = folder.listFiles()
        if (files != null) {
            for (file in files) {
                if (file.name != "profileInstalled") {
                    val lastModified = file.lastModified()
                    val currentDate = Date()
                    val diffInMillis = currentDate.time - lastModified
                    val diffInDays = diffInMillis / (24 * 60 * 60 * 1000)
                    if (diffInDays >= 7) {
                        file.delete()
//                        val isDeleted = file.delete()
//                        if (isDeleted) {
//                            println("Файл удален: " + file.name)
//                        } else {
//                            println("Не удалось удалить файл: " + file.name)
//                        }
                    }
                }
            }
        }
    }
    // Регулярка для поиска кодов
    fun extractCameraData(input: String): List<String> {
        val regex = Regex("\\d{19}[^}]{12}")
        val matches = regex.findAll(input)

        val result = mutableListOf<String>()
        for (match in matches) {
            val value = match.value.removePrefix("#STR#").removeSuffix("#STX#")
            result.add(value)
        }
        return result
    }
}