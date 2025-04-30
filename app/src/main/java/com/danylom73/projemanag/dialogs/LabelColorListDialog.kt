package com.danylom73.projemanag.dialogs

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.danylom73.projemanag.adapters.LabelColorListAdapter
import com.danylom73.projemanag.databinding.DialogListBinding

class LabelColorListDialog(
    private val activity: AppCompatActivity,
    private var list: ArrayList<String>,
    private val title: String = "",
    private var selectedColor: String = "",
    private var onColorSelected: (String) -> Unit
) : Dialog(activity) {
    private lateinit var binding: DialogListBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DialogListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setCanceledOnTouchOutside(true)
        setCancelable(true)
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        binding.tvTitle.text = title
        binding.rvList.layoutManager = LinearLayoutManager(activity)
        binding.rvList.adapter = LabelColorListAdapter(
            list, selectedColor
        ) { color ->
            this.dismiss()
            onColorSelected(color)
        }
    }
}