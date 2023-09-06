package com.digital_tent.cow_marks.list_job

data class Job (
    val product: String, // Продукт
    val gtin: String, // Gtin продукта
    val date: String, // Дата маркировки
    val fullDate: String, // Полная дата маркировки
    val expDate: String, // Конечная дата
    val party: String, // Партия
    val job: String, // Задание
    val tnvedCode: String, // ТНвед код
    var counter: String, // Счётчик(количество кодов в базе)
    var plan: String, // План производства
    var numberOfTheCodes: String, // Количество кодов для печати
    var file: String, // Ссылка на файл json
    var deleteStatus: Boolean, // Проверка на удаление
    var status: String, // Статус задания
    var color: Int // Цвет для статуса задания

)