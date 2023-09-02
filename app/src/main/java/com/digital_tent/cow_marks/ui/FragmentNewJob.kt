package com.digital_tent.cow_marks.ui

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import com.digital_tent.cow_marks.GlobalVariables
import com.digital_tent.cow_marks.databinding.FragmentNewJobBinding
import com.digital_tent.cow_marks.db.CodeDB
import com.digital_tent.cow_marks.db.CodeDao
import com.digital_tent.cow_marks.db.ProductDao
import com.digital_tent.cow_marks.json.JsonAndDate
import com.digital_tent.cow_marks.list_job.Job
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File


class FragmentNewJob(
    private val codeDB: CodeDao,
    private val productDB: ProductDao
) : Fragment() {
    // Список продуктов
    private lateinit var productList: Spinner
    private var productItems: List<String> = emptyList()

    // Глобальные переменные
    private lateinit var globalVariables: GlobalVariables

    // Кнопка создания задания
    private lateinit var buttonCreateJob: Button

    // Гарантированные поля для задания
    private lateinit var date: EditText
    private lateinit var plan: EditText
    private lateinit var party: EditText

    // Принтер
    private lateinit var printerText: TextView
    private lateinit var printerEdit: EditText

    // Класс для работы с датами и файлами
    private lateinit var jsonAndDate: JsonAndDate

    // Задание
    private lateinit var jobList: List<Job>
    private var jobWork: Int = 0
    private lateinit var job: Job
    private lateinit var jobProductName: String
    private lateinit var jobGtin: String
    private lateinit var jobWorkshop: String
    private lateinit var jobLine: String
    private lateinit var jobDateFull: String
    private lateinit var jobDate: String
    private lateinit var jobExpDate: String
    private lateinit var jobTNvedCode: String
    private lateinit var jobListCodes: List<String>
    private lateinit var jobCounter: String
    private var jobDateLife: Int = 0
    private lateinit var jobParty: String
    private lateinit var jobStatus: String
    private var jobColor: Int = 0
    private lateinit var jobFile: File
    private lateinit var jobPathFile: String

    // Количество кодов для задания
    private lateinit var jobCodesForPrinter: String

    // План производства
    private lateinit var jobPlan: String


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentNewJobBinding.inflate(inflater)
        // Инициализация глобальных переменных
        globalVariables = requireContext().applicationContext as GlobalVariables
        // Список продуктов
        productList = binding.newJobProductList
        // Кнопка создания задания
        buttonCreateJob = binding.newJobButtonCreate
        // Принтер
        printerText = binding.newJobCodesToThePrinterText
        printerEdit = binding.newJobCodesToThePrinterEdit
        // Дата маркировки
        date = binding.newJobDateEdit
        date.setText(globalVariables.getDateWork())
        // Партия маркировки
        party = binding.newJobPartyEdit
        // План производства
        plan = binding.newJobPlanEdit
        // Отображение строк для работы с принтером
        if (!globalVariables.getPrinterLine()) {
            printerText.isVisible = false
            printerEdit.isVisible = false
        }
        // Настройка задания
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Инициализация глобальных переменных
        globalVariables = requireContext().applicationContext as GlobalVariables
        jsonAndDate = JsonAndDate(requireContext())

        // Список продуктов
        CoroutineScope(Dispatchers.Main).launch {
            productItems = withContext(Dispatchers.IO) {
                productDB.getProductsByLine(globalVariables.getLine()).distinct()
            }
            val adapter =
                ArrayAdapter(requireActivity(), android.R.layout.simple_list_item_1, productItems)
            productList.adapter = adapter
            adapter.notifyDataSetChanged()
        }

        buttonCreateJob.setOnClickListener {

            // Настройка переменных для нового задания
            jobWork = globalVariables.getProductJob()
            jobProductName = productList.selectedItem.toString()
            jobWorkshop = globalVariables.getWorkshop()
            jobLine = globalVariables.getLine()
            // Поток для создания задания
            CoroutineScope(Dispatchers.Main).launch {
                jobGtin = withContext(Dispatchers.IO) {
                    productDB.getGtinByProduct(jobProductName)
                }
                jobDateLife = withContext(Dispatchers.IO) {
                    productDB.getLifeByProduct(jobProductName)
                }
                // Начальная дата
                jobDate = date.text.toString()
                // Общая дата с начальной и конечной
                jobDateFull = "${date.text} - ${
                    jsonAndDate.addExpirationDays(
                        date.text.toString(),
                        jobDateLife
                    )
                }"
                // Конечная дата
                jobExpDate =
                    jsonAndDate.addExpirationDays(date.text.toString(), jobDateLife).toString()
                // ТНвед код
                jobTNvedCode = withContext(Dispatchers.IO) {
                    productDB.getTnVedCode(jobProductName)
                }
                // Настройка партии
                jobParty = if (party.text.toString() == "") {
                    jsonAndDate.formatDate(jobDate)
                } else {
                    "${jsonAndDate.formatDate(jobDate)}-${party.text}"
                }
                jobStatus = "Новое"
                jobColor = Color.GREEN
                // Настройка плана и отлов пустоты
                jobPlan = if (plan.text.toString() == "") {
                    "10000"
                } else {
                    plan.text.toString()
                }
                jobPathFile = "${jobWorkshop}_${jobLine}_${jobGtin}_0_${jobParty}_${
                    jobDate
                }_${jobWork}.json"
                // Коды на принтер
                jobCodesForPrinter = if (globalVariables.getPrinterLine()) {
                    if (printerEdit.text.toString() == "") {
                        "10000"
                    } else {
                        printerEdit.text.toString()
                    }
                } else {
                    "0"
                }
                jobListCodes = withContext(Dispatchers.IO) {
                    codeDB.getCodes(jobGtin, jobWork.toString(), jobParty)
                }
                // Счётчик кодов
                jobCounter = jobListCodes.size.toString()

                job = Job(
                    jobProductName,
                    jobGtin,
                    jobDate,
                    jobDateFull,
                    jobExpDate,
                    jobParty,
                    jobWork.toString(),
                    jobTNvedCode,
                    jobCounter,
                    jobPlan,
                    jobCodesForPrinter,
                    jobPathFile,
                    jobStatus,
                    jobColor
                )
                jobList = globalVariables.getJobsList()
                val updateJobList = jobList.toMutableList()
                updateJobList.add(0, job)
                globalVariables.setJobsList(updateJobList)

                // Создание json
                val fileJson = jsonAndDate.createJsonFile(
                    context = requireContext(),
                    expDate = jobExpDate,
                    production_date = jobDate,
                    tnved_code = jobTNvedCode,
                    party = jobParty,
                    gtin = jobGtin,
                    listCodes = jobListCodes,
                    job = jobWork.toString(),
                    nameTerminal = globalVariables.getTerminalName(),
                    workshopFile = globalVariables.getWorkshop(),
                    plan = jobPlan,
                    status = jobStatus
                )

                // Отправка файла для ведения статистики заданий на линии
                jsonAndDate.uploadFileToServer(fileJson)

                // Удаление устаревших кодов
                val lifeCode = globalVariables.getLifeCode()
                val timeExpired = System.currentTimeMillis() / 1000 - (lifeCode * 24L * 60L * 60L)
                withContext(Dispatchers.IO) {
                    codeDB.deleteExpiredRows(timeExpired)
                }
                // Удаление устаревших файлов
                jsonAndDate.fileCheckerAndDelete()
//                println(lifeCode)
//                println(timeExpired)
//                println(System.currentTimeMillis() / 1000)
//                println(System.currentTimeMillis() / 1000 - (lifeCode * 24L * 60L * 60L))
//                println(codeDelete)
                requireActivity().runOnUiThread{
                    Toast.makeText(requireContext(), "Задание создано", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Увеличение счётчика Задания в глобальной переменной
        globalVariables.setProductJob((jobWork + 1))
        Toast.makeText(
            requireContext().applicationContext,
            "Задание создано",
            Toast.LENGTH_SHORT
        ).show()
    }


    companion object {
        @JvmStatic
        fun newInstance(
            codeDB: CodeDao,
            productDB: ProductDao,
        ) = FragmentNewJob(codeDB, productDB)
    }
}