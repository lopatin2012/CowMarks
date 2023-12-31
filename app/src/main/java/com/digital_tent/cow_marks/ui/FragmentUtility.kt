package com.digital_tent.cow_marks.ui

import android.app.Activity
import android.content.ContentValues
import android.graphics.Color
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
import com.digital_tent.cow_marks.databinding.FragmentUtilityBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.InetSocketAddress
import java.net.Socket

class FragmentUtility : Fragment() {

    // Глобальные переменные.
    private lateinit var globalVariables: GlobalVariables

    // Настройка глобального счётчика Заданий.
    private lateinit var globalJobEdit: EditText
    // Кнопка сохранения глобального счётчика заданий.
    private lateinit var buttonJobSave: Button

    // Настройки работы двух терминалов.
    private lateinit var buttonTwoTerminalSave: Button
    private lateinit var checkBoxTwoTerminal: CheckBox

    // Настройка сканирования с проверкой Gtin(строки).
    private lateinit var buttonCheckingGtin: Button
    private lateinit var checkBoxCheckingGtin: CheckBox

    // Фрагмент биндинг.
    private lateinit var binding: FragmentUtilityBinding

    // Камера
    private var buffer = ByteArray(1024) // Размер буфера
    private lateinit var cameraText: TextView
    private lateinit var buttonCameraJobsList: Button
    private lateinit var cameraJobsList: MutableList<String>
    private lateinit var cameraJobsListSpinner: Spinner

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
        // Кнопка сохранения глобального счётчика заданий
        buttonJobSave = binding.utilityUpdateGlobalJobButtonSave
        globalJobEdit.setText(globalVariables.getProductJob().toString())
        // Кнопка получения списка конфигураций с камеры
        buttonCameraJobsList = binding.utilityCameraJobsListButton
        // Текст камеры для изменения конфигуратора.
        cameraText = binding.utilityCameraJobsListText
        // Список конфигураций камеры
        cameraJobsListSpinner = binding.utilityCameraJobsList

        // Сканирование с двух терминалов.
        checkBoxTwoTerminal = binding.utilityTwoTerminalCheckBox
        buttonTwoTerminalSave = binding.utilityTwoTerminalButton
        checkBoxTwoTerminal.isChecked = globalVariables.getTwoScanning()

        // Сканирование с проверкой Gtin(строки).
        buttonCheckingGtin = binding.utilityCheckingGtinButton
        checkBoxCheckingGtin = binding.utilityCheckingGtinCheckBox
        checkBoxCheckingGtin.isChecked = globalVariables.getScanningGtin()

        buttonJobSave.setOnClickListener {
            globalVariables.setProductJob(globalJobEdit.text.toString().toInt())
            Toast.makeText(requireContext(), "Настройки сохранены", Toast.LENGTH_SHORT).show()
        }

        // Отключение кнопок
//        buttonTest.isEnabled = false
//        buttonSave.isEnabled = false
        buttonCameraJobsList.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                // Подключение к камере
                val ipCamera = globalVariables.getCameraIp()
                val portCamera = 1023
                try {
                    withContext(Dispatchers.IO) {
                        Socket(ipCamera, portCamera).use {
                            val output = it.getOutputStream()
                            val input = it.getInputStream()
                            // Получить название выбранного элемента, и очистка от спец символов
                            val configurationCamera =
                                cameraJobsListSpinner.selectedItem.toString().replace("\n", "")
                                    .replace("\r", "")
                            Log.e(configurationCamera, "onViewCreated:$configurationCamera")
                            // Режим хоста
                            output.write(byteArrayOf(27, 91, 67)) // <ESC> [ C
                            output.flush()
                            Log.e(
                                ContentValues.TAG,
                                "run: ${String(buffer, 0, input.read(buffer))}"
                            )
                            // Программирование камеры
                            output.write(byteArrayOf(27, 91, 66)) // <ESC> [ B
                            output.flush()
                            Log.e(
                                ContentValues.TAG,
                                "run: ${String(buffer, 0, input.read(buffer))}"
                            )
                            // Изменить текущую конфигурацию
                            output.write("CHANGE_CFG $configurationCamera\n".toByteArray()) // CHANGE_CFG<space>configuration_name <LF>
                            output.flush()
                            cameraJobsList =
                                String(buffer, 0, input.read(buffer)).splitToSequence("\n")
                                    .toMutableList()
                            Log.e(ContentValues.TAG, "run: $cameraJobsList")
                            // Изменить стартовую конфигурацию
                            output.write("STARTUP_CFG $configurationCamera\n".toByteArray()) // STARTUP_CFG<space>configuration_name <LF>
                            output.flush()
                            cameraJobsList =
                                String(buffer, 0, input.read(buffer)).splitToSequence("\n")
                                    .toMutableList()
                            Log.e(ContentValues.TAG, "run: $cameraJobsList")
                            output.write(byteArrayOf(27, 91, 65)) // <ESC> [ A
                            output.flush()
                            Log.e(
                                ContentValues.TAG,
                                "run: ${String(buffer, 0, input.read(buffer))}"
                            )
                            requireActivity().runOnUiThread {
                                Toast.makeText(
                                    requireContext(),
                                    "Конфигурация камеры изменена",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e(ContentValues.TAG, "Ошибка при отправке команды: ${e.message}")
                }
            }
        }

        // Режим работы от одного или двух терминалов.
        buttonTwoTerminalSave.setOnClickListener {
            globalVariables.setTwoScanning(checkBoxTwoTerminal.isChecked)
        }

        // Режим проверки Gtin(строки).
        buttonCheckingGtin.setOnClickListener {
            globalVariables.setScanningGtin(checkBoxCheckingGtin.isChecked)
        }
    }

    override fun onResume() {
        super.onResume()
        CoroutineScope(Dispatchers.IO).launch {
            // Подключение к камере
            val ipCamera = globalVariables.getCameraIp()
            // Специальный порт для взаимодействия с камерой через socket.
            val portCamera = 1023
            // Создание сокета для подключения к камере.
            val socket = Socket()
            try {
                withContext(Dispatchers.IO) {
                    socket.connect(InetSocketAddress(ipCamera, portCamera), 1000)
                    socket.use {
                        withContext(Dispatchers.Main) {
                            cameraText.setTextColor(Color.GREEN)
                        }
                        val output = it.getOutputStream()
                        val input = it.getInputStream()
                        // Режим хоста
                        output.write(byteArrayOf(27, 91, 67)) // <ESC> [ C
                        output.flush()
                        Log.e(ContentValues.TAG, "run: ${String(buffer, 0, input.read(buffer))}")
                        // Программирование камеры
                        output.write(byteArrayOf(27, 91, 66)) // <ESC> [ B
                        output.flush()
                        Log.e(ContentValues.TAG, "run: ${String(buffer, 0, input.read(buffer))}")
                        // Получить список заданий
                        output.write("GET_JOBS_LIST\n".toByteArray()) // GET_JOBS_LIST <LF>
                        output.flush()
                        cameraJobsList = String(buffer, 0, input.read(buffer)).splitToSequence("\n")
                            .toMutableList()
                        Log.e(ContentValues.TAG, "run: $cameraJobsList")
                        // Получить информацию о камере
//                            output.write("GET_INFO\n".toByteArray()) // GET_INFO<CR><LF>
//                            output.flush()
//                            Log.e(ContentValues.TAG, "run: ${String(buffer, 0, input.read(buffer))}")
                        output.write(byteArrayOf(27, 91, 65)) // <ESC> [ A
                        output.flush()
                        Log.e(ContentValues.TAG, "run: ${String(buffer, 0, input.read(buffer))}")
                        val firstItemInList = cameraJobsList.first()
                        val lastItemInList = cameraJobsList.last()
                        cameraJobsList.remove(firstItemInList)
                        cameraJobsList.remove(lastItemInList)
                        requireActivity().runOnUiThread {
                            val adapter = ArrayAdapter(
                                requireActivity(),
                                android.R.layout.simple_list_item_1,
                                cameraJobsList
                            )
                            cameraJobsListSpinner.adapter = adapter
                            adapter.notifyDataSetChanged()
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(ContentValues.TAG, "Ошибка при отправке команды: ${e.message}")
                withContext(Dispatchers.Main) {
                    cameraText.text = "Ошибка подключения"
                    cameraText.setBackgroundColor(Color.RED)
                }
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = FragmentUtility()
    }
}