package com.digital_tent.cow_marks

import android.app.Application
import android.content.SharedPreferences
import com.digital_tent.cow_marks.camera.TwoScanning
import com.digital_tent.cow_marks.databinding.FragmentFactoryBinding
import com.digital_tent.cow_marks.list_job.Job
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class GlobalVariables: Application() {
    // Сохранение переменных
    private lateinit var sharedPreferences: SharedPreferences
    // Редактирование переменных
    private lateinit var editor: SharedPreferences.Editor
    private lateinit var scanCodeThread: Thread
    // Тип списка заданий
    private val jobsList = mutableListOf<Job>()

    // Переопределение метода onCreate() от базового Application
    override fun onCreate() {
        super.onCreate()
        // Только приложение имеет доступ к файлу с переменными
        sharedPreferences = getSharedPreferences("global_variables", MODE_PRIVATE)
        // Необходимо для изменения переменных
        editor = sharedPreferences.edit()
    }
    // 1. Параметры для камеры
    // 2. Параметры для принтера
    // 3. Параметры для терминала
    // 4. Параметры для создания заданий на линии
    // 5. Параметры для открытия и закрытия заданий на линии

    // Параметры для открытия и закрытия заданий на линии
    //----------------------------------------------------------------------------------------------
    // Получить ip-адрес камеры
    fun getCameraIp(): String {
        return sharedPreferences.getString("camera_ip", "") ?: ""
    }
    // Сохранить ip-адрес камеры
    fun setCameraIp(cameraIp: String) {
        editor.putString("camera_ip", cameraIp)
        editor.apply()
    }
    //------------
    // Получить порт камеры
    fun getCameraPort(): Int {
        return sharedPreferences.getInt("camera_port", 9999)
    }
    // Сохранить порт камеры
    fun setCameraPort(cameraPort: Int) {
        editor.putInt("camera_port2", cameraPort)
        editor.apply()
    }
    //-----------------------------
    // Если нужно подключить 2 разных cbx
    // Получить ip-адрес камеры №2
    fun getCameraIp2(): String {
        return sharedPreferences.getString("camera_ip2", "") ?: ""
    }
    // Сохранить ip-адрес камеры №2
    fun setCameraIp2(cameraIp: String) {
        editor.putString("camera_ip2", cameraIp)
        editor.apply()
    }
    //------------
    // Получить порт камеры №2
    fun getCameraPort2(): Int {
        return sharedPreferences.getInt("camera_port2", 9999)
    }
    // Сохранить порт камеры №2
    fun setCameraPor2(cameraPort: Int) {
        editor.putInt("camera_port2", cameraPort)
        editor.apply()
    }
    //------------
    // Получить флаг сканирования по gtin
    fun getScanningGtin(): Boolean {
        return sharedPreferences.getBoolean("scanning_gtin", false)
    }
    // Сохранить флаг сканирования по gtin
    fun setScanningGtin(scanningGtin: Boolean) {
        editor.putBoolean("scanning_gtin", scanningGtin)
        editor.apply()
    }
    // Получать флаг сканирования
    fun getScanning(): Boolean {
        return sharedPreferences.getBoolean("scanning", false)
    }
    // Сохранить флаг сканирования
    fun setScanning(scanning: Boolean) {
        editor.putBoolean("scanning", scanning)
        editor.apply()
    }
    // Получить флаг на сканирование с двух разных cbx
    fun getTwoScanning(): Boolean {
        return sharedPreferences.getBoolean("two_scanning", false)
    }
    // Сохранить флаг на сканирование с двух разных cbx
    fun setTwoScanning(twoScanning: Boolean) {
        editor.putBoolean("two_scanning", twoScanning)
        editor.apply()
    }
    // Параметры для принтера
    //----------------------------------------------------------------------------------------------
    // Получить ip-адрес принтера
    fun getPrinterIp(): String {
        return sharedPreferences.getString("printer_ip", "") ?: ""
    }
    // Сохранить ip-адрес принтера
    fun setPrinterIp(printerIp: String) {
        editor.putString("printer_ip", printerIp)
        editor.apply()
    }
    //------------
    // Получить порт принтера
    fun getPrinterPort(): Int {
        return sharedPreferences.getInt("printer_port", 20111)
    }
    // Сохранить порт принтера
    fun setPrinterPort(printerPort: Int) {
        editor.putInt("printer_port", printerPort)
        editor.apply()
    }
    // Получить наличие принтера на линии
    fun getPrinterLine(): Boolean {
        return sharedPreferences.getBoolean("printer_line", false)
    }
    // Сохранить наличие принтера на линии
    fun setPrinterLine(printerLine: Boolean) {
        editor.putBoolean("printer_line", printerLine)
        editor.apply()
    }
    // Получить шаблон для печати на принтере
    fun getTemplate(): String {
        return sharedPreferences.getString("printer_template", "")?: ""
    }
    // Сохранить шаблон для печати на принтере
    fun setTemplate(template: String) {
        editor.putString("printer_template", template)
        editor.apply()
    }
    // Параметры для терминала
    //----------------------------------------------------------------------------------------------
    // Получить порт терминала
    fun getTerminalPort(): Int {
        return sharedPreferences.getInt("terminal_port", 10001)
    }
    // Сохранить порт терминала
    fun setTerminalPort(terminalPort: Int) {
        editor.putInt("terminal_port", terminalPort)
        editor.apply()
    }
    //------------
    // Получить имя терминала
    fun getTerminalName(): String {
        return sharedPreferences.getString("terminal_name", "Не задано")?: ""
    }
    // Сохранить имя терминала
    fun setTerminalName(terminalName: String) {
        editor.putString("terminal_name", terminalName)
        editor.apply()
    }
    //------------
    // Получить время жизни кода в базе данных(В днях)
    fun getLifeCode(): Int {
        return sharedPreferences.getInt("life_code", 3)
    }
    // Сохранить время жизни кода в базе данных(В днях)
    fun setLifeCode(lifeCode: Int) {
        editor.putInt("life_code", lifeCode)
        editor.apply()
    }
    //------------
    // Получить цех
    fun getWorkshop(): String {
        return sharedPreferences.getString("workshop", "")?: "Не задано"
    }
    // Сохранить цех
    fun setWorkshop(workshop: String) {
        editor.putString("workshop", workshop)
        editor.apply()
    }
    //------------
    // Получить линию
    fun getLine(): String {
        return sharedPreferences.getString("line", "")?: "Не задано"
    }
    // Сохранить линию
    fun setLine(line: String) {
        editor.putString("line", line)
        editor.apply()
    }
    // Параметры для создания заданий на линии
    //----------------------------------------------------------------------------------------------
    // Получить название продукта
    fun getProductName(): String {
        return sharedPreferences.getString("product_name", "")?: "Не задано"
    }
    // Сохранить название продукта
    fun setProductName(productName: String) {
        editor.putString("product_name", productName)
        editor.apply()
    }
    //------------
    // Получить gtin продукта
    fun getProductGtin(): String {
        return sharedPreferences.getString("product_gtin", "")?: "Не задано"
    }
    // Сохранить gtin продукта
    fun setProductGtin(productGtin: String) {
        editor.putString("product_gtin", "")
        editor.apply()
    }
    //------------
    // Получить дату
    fun getProductDate(): String {
        return sharedPreferences.getString("product_date", "")?: "Не задано"
    }
    // Сохранить дату
    fun setProductDate(productDate: String) {
        editor.putString("product_date", productDate)
        editor.apply()
    }
    // Получить финальную дату
    fun getProductDateFinal(): String {
        return sharedPreferences.getString("product_date_final", "")?: "Не задано"
    }
    // Сохранить финальную дату
    fun setProductDateFinal(productDateFinal: String) {
        editor.putString("product_date_final", productDateFinal)
        editor.apply()
    }
    //------------
    // Получить партию
    fun getProductParty(): String {
        return sharedPreferences.getString("product_party", "")?: "Не задано"
    }
    // Сохранить партию
    fun setProductParty(productParty: String) {
        editor.putString("product_party", productParty)
        editor.apply()
    }
    //------------
    // Получить номер глобального задания
    fun getProductJob(): Int {
        return sharedPreferences.getInt("product_job", 1)
    }
    // Сохранить номер глобального задания
    fun setProductJob(productJob: Int) {
        editor.putInt("product_job", productJob)
        editor.apply()
    }
    //------------
    // Получить номер выбранного задания
    fun getProductWorkJob(): Int {
        return sharedPreferences.getInt("product_work_job", 1)
    }
    // Сохранить номер выбранного задания
    fun setProductWorkJob(productJob: Int) {
        editor.putInt("product_work_job", productJob)
        editor.apply()
    }
    //------------
    // Получить ссылку на файл
    fun getFileJson(): String {
        return sharedPreferences.getString("file_json", "")?: "Не задано"
    }
    // Сохранить ссылку на файл
    fun setFileJson(fileJson: String) {
        editor.putString("file_json", fileJson)
        editor.apply()
    }
    // Получить список заданий
    fun getJobsList(): List<Job> {
        val json = sharedPreferences.getString("jobs_list", null)
        return if (json != null) {
            val gson = Gson()
            val type = object : TypeToken<List<Job?>?>() {}.type
            gson.fromJson<List<Job>>(json, type)
        } else {
            ArrayList<Job>()
        }
    }
    // Сохранение задания в список
    fun setJobsList(jobsList: List<Job>) {
        val gson = Gson()
        val json = gson.toJson(jobsList)
        editor.putString("jobs_list", json)
        editor.apply()
    }
    // Параметры для открытия и закрытия заданий на линии
    //----------------------------------------------------------------------------------------------
    // Получить имя продукта
    fun getProductWork(): String {
        return sharedPreferences.getString("product_work", "")?: "Не задано"
    }
    // Сохранить имя продукта
    fun setProductWork(productWork: String) {
        editor.putString("product_work", productWork)
        editor.apply()
    }
    // Получить GTIN продукта
    fun getGtinWork(): String {
        return sharedPreferences.getString("gtin_work", "")?: "Не задано"
    }
    // Сохранить Gtin продукта
    fun setGtinWork(gtinWork: String) {
        editor.putString("gtin_work", gtinWork)
        editor.apply()
    }
    // Получить дату маркировки
    fun getDateWork(): String {
        return sharedPreferences.getString("date_work", "")?: "Не задано"
    }
    // Сохранить дату маркировки
    fun setDateWork(dateWork: String) {
        editor.putString("date_work", dateWork)
        editor.apply()
    }
    // Получить полную дату
    fun getDateFullWork(): String {
        return sharedPreferences.getString("date_full_work", "")?: "Не задано"
    }
    // Сохранить полную дату
    fun setDateFullWork(dateFullWork: String) {
        editor.putString("date_full_work", dateFullWork)
        editor.apply()
    }
    // Получить ТНвед код
    fun getTNvedCode(): String {
        return sharedPreferences.getString("tnved_code_work", "")?: "Не задано"
    }
    // Сохранить ТНвед код
    fun setTNvedCode(tnVedCode: String) {
        editor.putString("tnved_code_work", tnVedCode)
        editor.apply()
    }
    // Получить конечную дату
    fun getExpDateWork(): String {
        return sharedPreferences.getString("date_exp_work", "")?: "Не задано"
    }
    // Сохранить конечную дату
    fun setExpDateWork(expDateWork: String) {
        editor.putString("date_exp_work", expDateWork)
        editor.apply()
    }
    // Получить партию
    fun getPartyWork(): String {
        return sharedPreferences.getString("party_work", "")?: "Не задано"
    }
    // Сохранить партию
    fun setPartyWork(partyWork: String) {
        editor.putString("party_work", partyWork)
        editor.apply()
    }
    // Получить план производства
    fun getPlanWork(): String {
        return sharedPreferences.getString("plan_work", "")?: "Не задано"
    }
    // Сохранить план производства
    fun setPlanWork(planWork: String) {
        editor.putString("plan_work", planWork)
        editor.apply()
    }
    // Получить статус задания
    fun getStatusWork(): String {
        return sharedPreferences.getString("status_work", "Не задано")?: "Не задано"
    }
    // Сохранить статус задания
    fun setStatusWork(statusWork: String) {
        editor.putString("status_work", statusWork)
        editor.apply()
    }
    // Получить номер задания
    fun getJobWork(): String {
        return sharedPreferences.getString("job_work", "0")?: "0"
    }
    // Сохранить номер задания
    fun setJobWork(jobWork: String) {
        editor.putString("job_work", jobWork)
        editor.apply()
    }
    // Получить текущую позицию в списке заданий
    fun getPositionJobInList(): Int {
        return sharedPreferences.getInt("position_job_in_list", 0)
    }
    // Сохранить текущую позицию в списке заданий
    fun setPositionJobInList(positionJobInList: Int) {
        editor.putInt("position_job_in_list", positionJobInList)
        editor.apply()
    }
    // Получить глобальный счётчик
    fun getCounter(): String {
        return sharedPreferences.getString("counter", "0")?: "0"
    }
    // Сохранить глобальный счётчик
    fun setCounter(counter: String) {
        editor.putString("counter", counter)
        editor.apply()
    }
}