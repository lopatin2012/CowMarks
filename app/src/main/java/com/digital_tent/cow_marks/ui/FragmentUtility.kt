package com.digital_tent.cow_marks.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.digital_tent.cow_marks.GlobalVariables
import com.digital_tent.cow_marks.R
import com.digital_tent.cow_marks.databinding.FragmentUtilityBinding

class FragmentUtility : Fragment() {

    // Глобальные переменные
    private lateinit var globalVariables: GlobalVariables

    private lateinit var globalJobEdit: EditText
    private lateinit var buttonSave: Button
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

        buttonSave.setOnClickListener {
            globalVariables.setProductJob(globalJobEdit.text.toString().toInt())
            Toast.makeText(requireContext(), "Настройки сохранены", Toast.LENGTH_SHORT).show()
        }


    }

    companion object {
        @JvmStatic
        fun newInstance() = FragmentUtility()
    }
}