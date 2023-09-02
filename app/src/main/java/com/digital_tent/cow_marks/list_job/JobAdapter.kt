package com.digital_tent.cow_marks.list_job

import android.content.ContentValues.TAG
import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.constraintlayout.helper.widget.MotionEffect
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.digital_tent.cow_marks.GlobalVariables
import com.digital_tent.cow_marks.MainActivity
import com.digital_tent.cow_marks.R
import com.digital_tent.cow_marks.databinding.JobItemBinding
import com.digital_tent.cow_marks.db.CodeDB
import com.digital_tent.cow_marks.db.CodeDao
import com.digital_tent.cow_marks.json.JsonAndDate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import java.io.File
import java.io.IOException


class JobAdapter(
    private var globalVariables: GlobalVariables,
    private var jobList: List<Job>,
    private var context: Context,
    private var codeDB: CodeDao,
    private var supportFragmentManager: FragmentManager
) :
    RecyclerView.Adapter<JobAdapter.JobsHolder>() {

    class JobsHolder(item: View) : RecyclerView.ViewHolder(item) {
        private val binding = JobItemBinding.bind(item)
        @OptIn(DelicateCoroutinesApi::class)
        fun bind(
            job: Job,
            globalVariables: GlobalVariables,
            jobList: List<Job>,
            context: Context,
            codeDB: CodeDao,
            supportFragmentManager: FragmentManager,
            adapter: JobAdapter
        ) = with(binding) {

            val jsonAndDate = JsonAndDate(context)
            listJobProductText.text =
                root.context.getString(R.string.list_job_product_text, job.product)
            listJobDateText.text = root.context.getString(R.string.list_job_date_text, job.date)
            listJobPartyText.text = root.context.getString(R.string.list_job_party_text, job.party)
            listJobCounterText.text =
                root.context.getString(R.string.list_job_counter_text, job.counter, job.plan)
            listJobCodesForPrinterText.text =
                root.context.getString(R.string.list_job_codes_for_printer, job.numberOfTheCodes)
            listJobStatusText.text =
                root.context.getString(R.string.list_job_status_text, job.job, job.status)
            listJobColor.setBackgroundColor(job.color)
            listJobButtonOpen.setOnClickListener {
                Log.e(TAG, "Задание открыто")
                val position: Int = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val currentJob = jobList[position]
                    globalVariables.setProductWork(currentJob.product)
                    globalVariables.setGtinWork(currentJob.gtin)
                    globalVariables.setJobWork(currentJob.job)
                    globalVariables.setDateWork(currentJob.date)
                    globalVariables.setDateFullWork(currentJob.fullDate)
                    globalVariables.setExpDateWork(currentJob.expDate)
                    globalVariables.setTNvedCode(currentJob.tnvedCode)
                    globalVariables.setPartyWork(currentJob.party)
                    globalVariables.setPlanWork(currentJob.plan)
                    globalVariables.setStatusWork(currentJob.status)
                    // Сохранить позицию задания в глобальной переменной
                    globalVariables.setPositionJobInList(position)
                    val statusOpen = "На линии"
                    val statusOpenColor = Color.CYAN
                    adapter.updateData(
                        position,
                        currentJob.counter,
                        statusOpen,
                        statusOpenColor,
                        currentJob.file
                    )
                    for (i in jobList.indices) {
                        val unitJob: Job = jobList[i]
                        if (unitJob.status == "На линии" && i != position) {
                            adapter.updateData(
                                i,
                                currentJob.counter,
                                "Открыто",
                                Color.YELLOW,
                                currentJob.file
                            )
                        }
                    }
                }
                (context as MainActivity).fragmentFactory()
            }
            listJobButtonClose.setOnClickListener {
                val position: Int = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    CoroutineScope(Dispatchers.Main).launch {
                        val currentJob = jobList[position]
                        val statusOpen = "Выгружено"
                        val statusOpenColor = Color.RED
                        val counter = withContext(Dispatchers.IO) {
                            CodeDB.getDB(context).codeDao().getCodes(
                                currentJob.gtin,
                                currentJob.job,
                                currentJob.party
                            ).size.toString()
                        }
                        val editFile = jsonAndDate.renameReport(currentJob.file)
//                        Log.e(TAG, "bind: $counter")
                        adapter.updateData(
                            position,
                            counter,
                            statusOpen,
                            statusOpenColor,
                            editFile
                        )
                        GlobalScope.launch(Dispatchers.IO) {
                            jsonAndDate.uploadFileToServer(editFile)
                        }
                    }
                }
            }
        }
    }

    // Создание элемента
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JobsHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.job_item, parent, false)
        return JobsHolder(view)
    }

    // Получить размер списка листа
    override fun getItemCount(): Int {
        return jobList.size
    }

    // Предоставить элемент по его позиции
    override fun onBindViewHolder(holder: JobsHolder, position: Int) {
        holder.bind(jobList[position], globalVariables, jobList, context, codeDB, supportFragmentManager, this)
    }

    // Удаление одного задания
    fun removeItem(position: Int) {
        if (position >= 0 && position < jobList.size) {
            val updatedList = jobList.toMutableList()
            updatedList.removeAt(position)
            globalVariables.setJobsList(updatedList)
            notifyItemChanged(position)
        }
    }

    // Обновление данных
    fun updateData(position: Int, counter: String, status: String, color: Int, file: String) {
        if (position >= 0 && position < jobList.size) {
            jobList[position].counter = counter
            jobList[position].status = status
            jobList[position].color = color
            jobList[position].file = file
            globalVariables.setJobsList(jobList)
            notifyItemChanged(position)
        }
    }
}