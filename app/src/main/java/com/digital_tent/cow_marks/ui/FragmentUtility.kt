package com.digital_tent.cow_marks.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.datalogic.decode.BarcodeManager
import com.datalogic.decode.configuration.ScannerOptions
import com.datalogic.decode.configuration.ScannerProperties
import com.datalogic.device.configuration.ConfigurationManager
import com.datalogic.device.configuration.NumericProperty
import com.digital_tent.cow_marks.GlobalVariables
import com.digital_tent.cow_marks.R
import com.digital_tent.cow_marks.databinding.FragmentUtilityBinding

class FragmentUtility : Fragment() {

    // Глобальные переменные.
    private lateinit var globalVariables: GlobalVariables

    // Настройка глобального счётчика Заданий.
    private lateinit var globalJobEdit: EditText
    // Кнопка сохранения настроек
    private lateinit var buttonSave: Button
    // Фрагмент биндинг.
    private lateinit var binding: FragmentUtilityBinding
    // Настройки камеры.
    private lateinit var configurationManager: ConfigurationManager
    private lateinit var configurationDecode: ScannerProperties
    private lateinit var configurationDevice: NumericProperty


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

        buttonSave.setOnClickListener {
            globalVariables.setProductJob(globalJobEdit.text.toString().toInt())
            Toast.makeText(requireContext(), "Настройки сохранены", Toast.LENGTH_SHORT).show()
        }

        // Создание Баркода Менеджера.
        configurationManager = ConfigurationManager(requireContext())
        // Создание класса сканера для внесения изменений
        val properties = NumericProperty(1, 6, 6)




    }

    companion object {
        @JvmStatic
        fun newInstance() = FragmentUtility()
    }
}