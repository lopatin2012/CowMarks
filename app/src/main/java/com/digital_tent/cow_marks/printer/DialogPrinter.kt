package com.digital_tent.cow_marks.printer

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.DialogFragment
import com.digital_tent.cow_marks.GlobalVariables
import com.digital_tent.cow_marks.databinding.FragmentFactoryBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.log

class DialogPrinter(factoryBinding: FragmentFactoryBinding, contextForPrinter: Context): DialogFragment() {

    private var partyShort = "100"
    val factoryBinding = factoryBinding
    val contextForPrinter = contextForPrinter

    companion object {
        private const val ARG_ITEMS = "items"

        fun newInstance(items: Array<String>, factoryBinding: FragmentFactoryBinding,
                        contextForPrinter: Context): DialogPrinter {
            val dialog = DialogPrinter(factoryBinding, contextForPrinter)
            val args = Bundle().apply {
                putStringArray(ARG_ITEMS, items)
            }
            dialog.arguments = args
            return dialog
        }
    }
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val items = requireArguments().getStringArray(ARG_ITEMS) ?: emptyArray<String>()
        val globalVariables = requireContext().applicationContext as GlobalVariables
        val codePrinting = CodePrinting(globalVariables, contextForPrinter)
        val dateWork = globalVariables.getDateWork()
        val dateExpWork = globalVariables.getExpDateWork()
        val partyWork = globalVariables.getPartyWork()

        val alertDialogBuilder = AlertDialog.Builder(requireActivity())
        alertDialogBuilder.setTitle("Выберите шаблон")

        alertDialogBuilder.setSingleChoiceItems(items, -1) {
            _, isChecked ->
            // Сохраняем имя шаблона в глобальную переменную при выборе.
            globalVariables.setTemplate(items[isChecked])
        }

        // Позитивная кнопка
        alertDialogBuilder.setPositiveButton("Выбрать") {
            dialog: DialogInterface, _: Int ->
            // Обработка позитивного события
            globalVariables.setPrinting(true)
            // Ловим ошибку по партии, если она отсутствует.
            // Есть потенциальная ошибка, если партия более 3-х символов.
            // То принтер уйдёт в печать без ограничений шаблонного кода.
            partyShort = try {
                partyWork.split("-")[1]
            } catch (e: Exception) {
                "100"
            }
            // Установить шаблон в текстовое поле.
            requireActivity().runOnUiThread {
                factoryBinding.factoryPrinterTemplate.text = globalVariables.getTemplate()
            }
            CoroutineScope(Dispatchers.IO).launch {
                codePrinting.printingCodes(dateWork, dateExpWork, partyShort)
            }
            dialog.dismiss()
        }
        // Негативная кнопка
        alertDialogBuilder.setNegativeButton("Отмена") {
            dialog: DialogInterface, _: Int ->
            // Обработка негативного события
            dialog.dismiss()
        }

        return alertDialogBuilder.create()


    }
}