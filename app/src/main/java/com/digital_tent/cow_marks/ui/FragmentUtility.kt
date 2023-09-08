package com.digital_tent.cow_marks.ui

import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.digital_tent.cow_marks.GlobalVariables
import com.digital_tent.cow_marks.databinding.FragmentUtilityBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.Socket

class FragmentUtility : Fragment() {

    // Глобальные переменные.
    private lateinit var globalVariables: GlobalVariables

    // Настройка глобального счётчика Заданий.
    private lateinit var globalJobEdit: EditText
    // Кнопка сохранения настроек
    private lateinit var buttonSave: Button
    //Тесты
    private lateinit var buttonTest: Button
    // Фрагмент биндинг.
    private lateinit var binding: FragmentUtilityBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentUtilityBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        globalVariables = requireContext().applicationContext as GlobalVariables
        globalJobEdit = binding.utilityUpdateGlobalJobEdit
        buttonSave = binding.utilityButtonSave
        globalJobEdit.setText(globalVariables.getProductJob().toString())
        // Для тестов.
        buttonTest = binding.utilityTestButton

        buttonSave.setOnClickListener {
            globalVariables.setProductJob(globalJobEdit.text.toString().toInt())
            Toast.makeText(requireContext(), "Настройки сохранены", Toast.LENGTH_SHORT).show()
        }

        buttonTest.setOnClickListener {
            Toast.makeText(requireContext(), "Нажата кнопка теста", Toast.LENGTH_SHORT).show()
            CoroutineScope(Dispatchers.IO).launch {
                withContext(Dispatchers.IO) {
                    Socket(globalVariables.getCameraIp(), globalVariables.getCameraPort()).use {
                        val output = it.getOutputStream()
                        val input = it.getInputStream()
                        output.write("SET<space>PSTR<space>VAL <LF>".toByteArray())
                        output.flush()
                        Log.e(ContentValues.TAG, "run: ${input.read()}")
                    }
                }
            }
        }

    }

    companion object {
        @JvmStatic
        fun newInstance() = FragmentUtility()
    }
}