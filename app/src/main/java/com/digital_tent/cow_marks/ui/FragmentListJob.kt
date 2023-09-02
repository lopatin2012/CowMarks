package com.digital_tent.cow_marks.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.digital_tent.cow_marks.GlobalVariables
import com.digital_tent.cow_marks.databinding.ActivityMainBinding
import com.digital_tent.cow_marks.databinding.FragmentFactoryBinding
import com.digital_tent.cow_marks.databinding.FragmentListJobBinding
import com.digital_tent.cow_marks.db.CodeDao
import com.digital_tent.cow_marks.db.ProductDao
import com.digital_tent.cow_marks.list_job.Job
import com.digital_tent.cow_marks.list_job.JobAdapter

class FragmentListJob(
    globalVariables: GlobalVariables,
    adapter: JobAdapter,
    private val supportFragmentManager: FragmentManager,
    private val codeDB: CodeDao
) :
    Fragment() {

    private lateinit var jobList: RecyclerView
    private lateinit var jobItems: List<Job>
    private lateinit var globalVariables: GlobalVariables
    private lateinit var adapter: JobAdapter
    private lateinit var binding: FragmentFactoryBinding
    private lateinit var productDB: ProductDao

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentListJobBinding.inflate(inflater)
        jobList = binding.listJobRecycler
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        globalVariables = requireContext().applicationContext as GlobalVariables
        jobItems = globalVariables.getJobsList()
        adapter = JobAdapter(
            globalVariables,
            jobItems,
            requireContext(),
            codeDB,
            supportFragmentManager
        )
        jobList.layoutManager = LinearLayoutManager(requireContext().applicationContext)
        jobList.adapter = adapter
    }

    companion object {
        @JvmStatic
        fun newInstance(
            globalVariables: GlobalVariables,
            adapter: JobAdapter,
            supportFragmentManager: FragmentManager,
            codeDB: CodeDao
        ) =
            FragmentListJob(globalVariables, adapter, supportFragmentManager, codeDB)
    }

}