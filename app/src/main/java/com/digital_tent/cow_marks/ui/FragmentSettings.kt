package com.digital_tent.cow_marks.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.digital_tent.cow_marks.GlobalVariables
import com.digital_tent.cow_marks.R
import com.digital_tent.cow_marks.databinding.FragmentSettingsBinding
import com.digital_tent.cow_marks.db.WorkshopDB
import com.digital_tent.cow_marks.db.WorkshopDao
import com.digital_tent.cow_marks.retrofit.WorkshopsJson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Arrays


class FragmentSettings(private val workshopDB: WorkshopDao) : Fragment() {
    // Список цехов
    private lateinit var workshopList: Spinner
    private var workshopItems: List<String> = emptyList()
    private lateinit var workshopName: TextView

    // Список линий
    private lateinit var lineList: Spinner
    private var lineItems: List<String> = emptyList()
    private lateinit var lineName: TextView

    // Кнопка сохранения
    private lateinit var buttonSave: Button
    private lateinit var buttonUpdate: Button

    // Камера
    private lateinit var cameraIp: EditText
    private lateinit var cameraPort: EditText
    private lateinit var cameraModeText: TextView
    private lateinit var cameraMode: Spinner

    // Принтер
    private lateinit var printerIp: EditText
    private lateinit var printerPort: EditText
    private lateinit var printerLine: CheckBox

    // Название терминала
    private lateinit var nameTerminal: TextView

    // Время жизни кода в базе терминала
    private lateinit var lifeCode: EditText

    // Глобальные переменные
    private lateinit var globalVariables: GlobalVariables

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentSettingsBinding.inflate(inflater)
        globalVariables = requireContext().applicationContext as GlobalVariables
        // Список цехов и линий
        workshopList = binding.settingsWorkshop
        lineList = binding.settingsLine
        // Кнопки
        buttonSave = binding.settingsButtonSave
        buttonUpdate = binding.settingsButtonUpdateData
        // Получение переменных для работы с полями настроек
        // Камера
        cameraIp = binding.settingsCameraIpAddressEdit
        cameraPort = binding.settingsCameraPortEdit
        cameraModeText = binding.settingsCameraModeText
        cameraMode = binding.settingsCameraModeList
        // Принтер
        printerIp = binding.settingsPrinterIpAddressEdit
        printerPort = binding.settingsPrinterPortEdit
        printerLine = binding.settingsPrinter
        // Терминал
        nameTerminal = binding.settingsNameTerminalText
        // Жизни кода в днях
        lifeCode = binding.settingsServerLifeTimeEdit
        // Цех
        workshopName = binding.settingsWorkshopText
        // Линия
        lineName = binding.settingsLineText

        // Установка сохранённых настроек
        // Камера
        cameraIp.setText(globalVariables.getCameraIp())
        cameraPort.setText(globalVariables.getCameraPort().toString())
        cameraModeText.text = globalVariables.getScanningMode()
        // Принтер
        printerIp.setText(globalVariables.getPrinterIp())
        printerPort.setText(globalVariables.getPrinterPort().toString())
        printerLine.isChecked = globalVariables.getPrinterLine()
        // Терминал
        nameTerminal.text = resources.getString(
            R.string.settings_name_terminal_text,
            globalVariables.getTerminalName()
        )
        lifeCode.setText(globalVariables.getLifeCode().toString())
        // Цех
        workshopName.text = resources.getString(R.string.settings_workshop_text, globalVariables.getWorkshop())
        // Линия
        lineName.text = resources.getString(R.string.settings_line_text, globalVariables.getLine())


        return binding.root
    }

    // Выбор цеха с изменением линий
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Формируем список режимов для камеры
        modeScanning()
        CoroutineScope(Dispatchers.Main).launch {
            val workshopDB = WorkshopDB.getDB(requireContext().applicationContext).workshopDao()
            workshopItems = withContext(Dispatchers.IO) {
                workshopDB.getWorkshops().distinct()
            }
            val adapter =
                ArrayAdapter(requireActivity(), android.R.layout.simple_list_item_1, workshopItems)
            workshopList.adapter = adapter
            adapter.notifyDataSetChanged()
        }
        workshopList.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedWorkshop = workshopItems[position]
                updateLineList(selectedWorkshop)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Если ничего не выбрано
            }
        }

        // Сохранение настроек
        buttonSave.setOnClickListener {
            // Камера
            globalVariables.setCameraIp(cameraIp.text.toString())
            globalVariables.setCameraPort(cameraPort.text.toString().toInt())
            globalVariables.setScanningMode(cameraMode.selectedItem.toString())
            cameraModeText.text = resources.getString(R.string.settings_camera_mode_text, globalVariables.getScanningMode())
            // Принтер
            globalVariables.setPrinterIp(printerIp.text.toString())
            globalVariables.setPrinterPort(printerPort.text.toString().toInt())
            globalVariables.setPrinterLine(printerLine.isChecked)
            // Терминал. Название терминала
            globalVariables.setTerminalName(lineList.selectedItem.toString())
            nameTerminal.text = resources.getString(
                R.string.settings_name_terminal_text,
                globalVariables.getTerminalName()
            )
            // Время жизни кода
            globalVariables.setLifeCode(lifeCode.text.toString().toInt())
            // Цех
            globalVariables.setWorkshop(workshopList.selectedItem.toString())
            workshopName.text = resources.getString(R.string.settings_workshop_text, globalVariables.getWorkshop())

            // Линия
            globalVariables.setLine(lineList.selectedItem.toString())
            lineName.text = resources.getString(R.string.settings_line_text, globalVariables.getLine())
            // Уведомление о выполненной операции
            Toast.makeText(requireContext(), "Настройки сохранены", Toast.LENGTH_SHORT).show()
        }

        // Обновление базы данных продуктов, линий и цехов
        buttonUpdate.setOnClickListener {
            WorkshopsJson(requireActivity()).connect()
            Toast.makeText(requireContext(), "База данных обновлена", Toast.LENGTH_SHORT).show()
        }
    }

    // Обновление списка линий в зависимости от цеха
    private fun updateLineList(selectedWorkshop: String) {
        CoroutineScope(Dispatchers.Main).launch {
            lineItems = withContext(Dispatchers.IO) {
                workshopDB.getLineByWorkshop(selectedWorkshop).distinct()
            }
            val adapter =
                ArrayAdapter(requireActivity(), android.R.layout.simple_list_item_1, lineItems)
            lineList.adapter = adapter
            adapter.notifyDataSetChanged()
            Log.e("Линии", lineItems.toString())
        }
    }

    private fun modeScanning() {
        CoroutineScope(Dispatchers.IO).launch {
            val cameraModeList = listOf<String>(*resources.getStringArray(R.array.settings_camera_mode))
            val adapter =
                ArrayAdapter(requireActivity(), android.R.layout.simple_list_item_1, cameraModeList)
            cameraMode.adapter = adapter
        }
    }

    companion object {

        @JvmStatic
        fun newInstance(workshopDB: WorkshopDao) = FragmentSettings(workshopDB)
    }
}