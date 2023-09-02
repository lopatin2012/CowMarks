package com.digital_tent.cow_marks.ui

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentManager
import androidx.navigation.ui.NavigationUI
import com.digital_tent.cow_marks.GlobalVariables
import com.digital_tent.cow_marks.MainActivity
import com.digital_tent.cow_marks.R
import com.digital_tent.cow_marks.camera.OneScanner
import com.digital_tent.cow_marks.camera.OneScannerCheck
import com.digital_tent.cow_marks.databinding.ActivityMainBinding
import com.digital_tent.cow_marks.databinding.FragmentFactoryBinding
import com.digital_tent.cow_marks.db.CodeDao
import com.digital_tent.cow_marks.json.JsonAndDate
import com.digital_tent.cow_marks.list_job.Job
import com.digital_tent.cow_marks.list_job.JobAdapter
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FragmentFactory(
    private var globalVariables: GlobalVariables,
    private val codeDB: CodeDao,
    private var supportFragmentManager: FragmentManager
) : Fragment() {

    // Кнопка завершения партии
    private lateinit var buttonEnd: Button

    // Кнопка синхронизации кодов
    private lateinit var buttonReset: ImageButton

    // Класс для создания json файлов
    private lateinit var jsonAndDate: JsonAndDate

    // Параметры задания. В основном для Json
    // Часть функционала есть в сканировании
    private lateinit var dateWork: String
    private lateinit var dateExpWork: String
    private lateinit var partyWork: String
    private lateinit var vetCodeWork: String
    private lateinit var gtinWork: String
    private lateinit var listCodeWork: List<String>

    // Переменные только для названия json файла
    private lateinit var jobWork: String
    private lateinit var terminalNameFile: String
    private lateinit var workshopNameFile: String

    // Переменные для удобства отчёта
    private lateinit var planWork: String
    private lateinit var statusWork: String

    private lateinit var binding: FragmentFactoryBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFactoryBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        globalVariables = requireContext().applicationContext as GlobalVariables
        // Функционал принтера
        if (!globalVariables.getPrinterLine()) {
            binding.factoryButtonStartPrinting.isVisible = false
            binding.factoryCodesOnThePtinter.isVisible = false
            binding.factoryNumbersOfCodes.isVisible = false
            binding.factoryPrinterTemplate.isVisible = false
        }
        // отображение текущего задания
        CoroutineScope(Dispatchers.IO).launch {
            val line = globalVariables.getLine()
            val gtin = globalVariables.getGtinWork()
            val job = globalVariables.getJobWork()
            val party = globalVariables.getPartyWork()
            binding.factoryJob.text = resources.getString(R.string.factory_job, line, job)
            binding.factoryProduct.text =
                resources.getString(R.string.factory_product, globalVariables.getProductWork())
            binding.factoryPlan.text =
                resources.getString(R.string.factory_plan, globalVariables.getPlanWork())
            binding.factoryDate.text =
                resources.getString(R.string.factory_date, globalVariables.getDateWork())
            binding.factoryParty.text =
                resources.getString(R.string.factory_party, globalVariables.getPartyWork())
        }
        buttonEnd = binding.factoryButtonEnd
        buttonEnd.isEnabled = false
        buttonReset = binding.factoryButtonReset

        // Кнопка синхронизации кодов
        buttonReset.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                activity?.runOnUiThread {
                    buttonEnd.text = "Сихнронизация кодов..."
                    buttonReset.isEnabled = false
                }
                partyWork = globalVariables.getPartyWork()
                gtinWork = globalVariables.getGtinWork()
                jobWork = globalVariables.getJobWork()
                globalVariables.setCounter(withContext(Dispatchers.IO) {
                    codeDB.getCodes(gtinWork, jobWork, partyWork).size.toString()
                })
                binding.factoryCounter.text = globalVariables.getCounter()
                activity?.runOnUiThread {
                    buttonEnd.text = "Завершить партию"
                    buttonReset.isEnabled = true
                    buttonEnd.isEnabled = true
                }
            }
        }

        // Кнопка завершения партии с созданием json файла
        buttonEnd.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                jsonAndDate = JsonAndDate(requireContext())
                println("Партия завершена")
                dateWork = globalVariables.getDateWork()
                dateExpWork = globalVariables.getExpDateWork()
                partyWork = globalVariables.getPartyWork()
                gtinWork = globalVariables.getGtinWork()
                jobWork = globalVariables.getJobWork()
                vetCodeWork = globalVariables.getTNvedCode()
                listCodeWork = withContext(Dispatchers.IO) {
                    codeDB.getCodes(
                        gtinWork,
                        jobWork,
                        partyWork
                    )
                }
                terminalNameFile = globalVariables.getTerminalName()
                workshopNameFile = globalVariables.getWorkshop()
                // План для задания
                planWork = globalVariables.getPlanWork()
                // Статус задания
                statusWork = globalVariables.getStatusWork()
                println(dateWork)
                println(dateExpWork)
                // Создаём json файл
                // Путь до файла json
                val jobPathFile = jsonAndDate.createJsonFile(
                    context = requireContext(),
                    expDate = dateExpWork,
                    production_date = dateWork,
                    tnved_code = vetCodeWork,
                    party = partyWork,
                    gtin = gtinWork,
                    listCodes = listCodeWork,
                    job = jobWork,
                    nameTerminal = terminalNameFile,
                    workshopFile = workshopNameFile,
                    plan = planWork,
                    status = "Завершено"
                )
                // Отправка файла на сайт для ведения отчёта
                jsonAndDate.uploadFileToServer(jobPathFile)
                val listJob: List<Job> = globalVariables.getJobsList()
                val adapter = JobAdapter(
                    globalVariables,
                    listJob,
                    requireContext(),
                    codeDB,
                    supportFragmentManager
                )
                adapter.updateData(
                    globalVariables.getPositionJobInList(),
                    listCodeWork.size.toString(),
                    "Завершено",
                    Color.GRAY,
                    jobPathFile
                )
                activity?.runOnUiThread {
                    buttonReset.isEnabled = false
                    buttonReset.setBackgroundColor(Color.GREEN)
                    buttonEnd.text = "Партия завершена"
                    Toast.makeText(requireContext(), "Партия завершена", Toast.LENGTH_SHORT).show()
                    buttonEnd.isEnabled = false
                    buttonEnd.setBackgroundColor(Color.GREEN)
                    buttonEnd.setTextColor(Color.BLACK)
                }

            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(
            globalVariables: GlobalVariables,
            codeDB: CodeDao,
            supportFragmentManager: FragmentManager,
        ) =
            FragmentFactory(globalVariables, codeDB, supportFragmentManager)
    }


    override fun onResume() {
        super.onResume()
        OneScannerCheck.startScan(requireContext(), requireActivity(), globalVariables, binding)
    }

    override fun onPause() {
        super.onPause()
        OneScannerCheck.stopScan(globalVariables)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        globalVariables = requireContext().applicationContext as GlobalVariables
    }
}