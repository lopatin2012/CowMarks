package com.digital_tent.cow_marks.camera

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import com.digital_tent.cow_marks.GlobalVariables
import com.digital_tent.cow_marks.databinding.FragmentFactoryBinding

// Один из способов использовать только один поток на фоне
object OneScanner {
    @SuppressLint("StaticFieldLeak")
    private lateinit var scanThread: OneCode

    @SuppressLint("SuspiciousIndentation")
    fun startScan(
        context: Context,
        activity: Activity,
        globalVariables: GlobalVariables,
        binding: FragmentFactoryBinding
    ) {
            scanThread = OneCode(context, activity, globalVariables, binding)
            globalVariables.setScanning(true)
                scanThread.start()

    }
    // Останавливаем и расщепляем поток на атомы
    fun stopScan(globalVariables: GlobalVariables,) {
        globalVariables.setScanning(false)
        scanThread.interrupt()
    }
}