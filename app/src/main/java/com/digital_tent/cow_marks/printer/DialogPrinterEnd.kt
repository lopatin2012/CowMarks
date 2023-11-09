package com.digital_tent.cow_marks.printer

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.digital_tent.cow_marks.GlobalVariables
import com.digital_tent.cow_marks.R
import com.digital_tent.cow_marks.databinding.FragmentFactoryBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class DialogPrinterEnd(fragmentFactoryBinding: FragmentFactoryBinding, contextForPrinter: Context): DialogFragment() {
    var binding = fragmentFactoryBinding
    val contextForPrinter = contextForPrinter
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            val globalVariables = requireContext().applicationContext as GlobalVariables
            val codePrinting = CodePrinting(globalVariables, contextForPrinter)

            builder.setTitle("Внимание!")
                .setMessage("Остановить печать?")
                .setCancelable(true)
                .setPositiveButton("Да") { _, _ ->
                    Log.e("Статус печати позитив: ", "Да")
                    Toast.makeText(activity, "Печать остановлена", Toast.LENGTH_SHORT).show()
                    globalVariables.setPrinting(false)
                    binding.factoryButtonStartPrinting.setBackgroundColor(ContextCompat.getColor(
                        requireActivity().applicationContext , R.color.violet))
                    CoroutineScope(Dispatchers.IO).launch {
                        codePrinting.clear()
                        delay(150)
                    }
                }
                .setNegativeButton("Нет") { _, _ ->
                    Log.e("Статус печати негатив: ", "Нет")
                }
            builder.create()
        } ?: throw IllegalStateException("Активити не может быть null")
    }

}