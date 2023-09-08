package com.digital_tent.cow_marks.camera

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import com.digital_tent.cow_marks.GlobalVariables
import com.digital_tent.cow_marks.databinding.FragmentFactoryBinding

object TwoScanner {
    @SuppressLint("StaticFieldLeak")
    private lateinit var scanThread: TwoScanning

    @SuppressLint("SuspiciousIndentation")
    fun startScan(
        context: Context,
        activity: Activity,
        globalVariables: GlobalVariables,
        binding: FragmentFactoryBinding
    ) {
        scanThread = TwoScanning(context, activity, globalVariables, binding)
        globalVariables.setScanning(true)
        scanThread.start()

    }
    // Останавливаем и расщепляем поток на атомы
    fun stopScan(globalVariables: GlobalVariables,) {
        globalVariables.setScanning(false)
        scanThread.interrupt()
    }
}