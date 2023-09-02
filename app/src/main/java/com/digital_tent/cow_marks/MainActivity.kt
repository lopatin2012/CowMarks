package com.digital_tent.cow_marks

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.get
import androidx.navigation.ui.NavigationUI
import com.digital_tent.cow_marks.databinding.ActivityMainBinding
import com.digital_tent.cow_marks.databinding.FragmentFactoryBinding
import com.digital_tent.cow_marks.db.CodeDB
import com.digital_tent.cow_marks.db.ProductDB
import com.digital_tent.cow_marks.db.WorkshopDB
import com.digital_tent.cow_marks.list_job.Job
import com.digital_tent.cow_marks.list_job.JobAdapter
import com.digital_tent.cow_marks.retrofit.WorkshopsJson
import com.digital_tent.cow_marks.ui.FragmentFactory
import com.digital_tent.cow_marks.ui.FragmentListJob
import com.digital_tent.cow_marks.ui.FragmentNewJob
import com.digital_tent.cow_marks.ui.FragmentSettings
import com.digital_tent.cow_marks.ui.FragmentUtility
import com.digital_tent.print_and_scan.ServerForTSD
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    private lateinit var globalVariables: GlobalVariables
    private lateinit var binding: ActivityMainBinding
    private lateinit var bindingFactory: FragmentFactoryBinding
    private lateinit var bindingMain: ActivityMainBinding
    private lateinit var bottomMenu: BottomNavigationView
    private lateinit var jobsList: List<Job>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        globalVariables = application as GlobalVariables
        binding = ActivityMainBinding.inflate(layoutInflater)
        bindingFactory = FragmentFactoryBinding.inflate(layoutInflater)
        bindingMain = ActivityMainBinding.inflate(layoutInflater)
        bottomMenu = bindingMain.bottomMenu
        jobsList = globalVariables.getJobsList()

        // Инициализация баз данных
        val workshopDB = WorkshopDB.getDB(this@MainActivity).workshopDao()
        val productDB = ProductDB.getDB(this@MainActivity).productDao()
        val codeDB = CodeDB.getDB(this@MainActivity).codeDao()
        val adapter = JobAdapter(
            globalVariables,
            jobsList,
            applicationContext,
            codeDB,
            supportFragmentManager
        )


        fragmentFactory()
//        binding.bottomMenu.selectedItemId = R.id.bottom_settings
        binding.bottomMenu.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.bottom_factory -> {
                    Log.d(TAG, "factory: нажата кнопка")
                    supportFragmentManager
                        .beginTransaction()
                        .replace(
                            R.id.fragment_main_menu,
                            FragmentFactory.newInstance(
                                globalVariables,
                                codeDB,
                                supportFragmentManager
                            )
                        )
                        .commit()
                }

                R.id.bottom_new_job -> {
                    supportFragmentManager
                        .beginTransaction()
                        .replace(
                            R.id.fragment_main_menu,
                            FragmentNewJob.newInstance(codeDB, productDB)
                        )
                        .commit()
                    Log.d(TAG, "new_job: нажата кнопка")
                }

                R.id.bottom_list_job -> {
                    supportFragmentManager
                        .beginTransaction()
                        .replace(
                            R.id.fragment_main_menu,
                            FragmentListJob.newInstance(
                                globalVariables,
                                adapter,
                                supportFragmentManager,
                                codeDB
                            )
                        )
                        .commit()
                    Log.d(TAG, "list_job: нажата кнопка")
                }

                R.id.bottom_utility -> {
                    supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.fragment_main_menu, FragmentUtility.newInstance())
                        .commit()
                    Log.d(TAG, "utility: нажата кнопка")
                }

                R.id.bottom_settings -> {
                    supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.fragment_main_menu, FragmentSettings.newInstance(workshopDB))
                        .commit()
                    Log.d(TAG, "settings: нажата кнопка")
                }
            }
            true
        }
        ServerForTSD(this@MainActivity, globalVariables).start()
//        WorkshopsJson(this@MainActivity).connect()
        setContentView(binding.root)
    }

    fun fragmentFactory() {
        val codeDB = CodeDB.getDB(this@MainActivity).codeDao()
        supportFragmentManager
            .beginTransaction()
            .replace(
                R.id.fragment_main_menu,
                FragmentFactory.newInstance(
                    globalVariables,
                    codeDB,
                    supportFragmentManager
                )
            )
            .commit()
        binding.bottomMenu.selectedItemId = R.id.bottom_factory
    }

}