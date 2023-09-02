package com.digital_tent.cow_marks.camera

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import com.digital_tent.cow_marks.GlobalVariables
import com.digital_tent.cow_marks.databinding.FragmentFactoryBinding

// Один из способов использовать только один поток на фоне
// Проверка кода в базе, а затем добавление в базу
object OneScannerCheck {
    @SuppressLint("StaticFieldLeak")
    private lateinit var scanThread: OneCodeCheck

    @SuppressLint("SuspiciousIndentation")
    fun startScan(
        context: Context,
        activity: Activity,
        globalVariables: GlobalVariables,
        binding: FragmentFactoryBinding
    ) {
        scanThread = OneCodeCheck(context, activity, globalVariables, binding)
        globalVariables.setScanning(true)
        scanThread.start()

    }
    // Останавливаем и расщепляем поток на атомы
    fun stopScan(globalVariables: GlobalVariables,) {
        globalVariables.setScanning(false)
        scanThread.interrupt()
    }
}