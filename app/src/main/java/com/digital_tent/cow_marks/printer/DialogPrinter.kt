package com.digital_tent.cow_marks.printer

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.DialogFragment
import com.digital_tent.cow_marks.GlobalVariables

class DialogPrinter: DialogFragment() {

    companion object {
        private const val ARG_ITEMS = "items"

        fun newInstance(items: Array<String>): DialogPrinter {
            val dialog = DialogPrinter()
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

        val alertDialogBuilder = AlertDialog.Builder(requireActivity())
        alertDialogBuilder.setTitle("Выберите шаблон")

        alertDialogBuilder.setMultiChoiceItems(items, null) {
            _, which, isChecked ->
            if (isChecked) {
                Log.d("Выбран элемент", items[which])
                globalVariables.setTemplate(items[which])
                dialog?.dismiss()
            }
        }

        // Позитивная кнопка
        alertDialogBuilder.setPositiveButton("Выбрать") {
            dialog: DialogInterface, _: Int ->
            // Обработка позитивного события
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