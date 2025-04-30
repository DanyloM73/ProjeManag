package com.danylom73.projemanag.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.danylom73.projemanag.databinding.ItemLabelColorBinding
import androidx.core.graphics.toColorInt

class LabelColorListAdapter(
    private val colors: ArrayList<String>,
    private val selectedColor: String,
    private val onClick: (String) -> Unit
) : RecyclerView.Adapter<LabelColorListAdapter.ColorListViewHolder>() {

    inner class ColorListViewHolder(private val binding: ItemLabelColorBinding)
        : RecyclerView.ViewHolder(binding.root) {

        fun bind(color: String) {
            binding.viewMain.setBackgroundColor(color.toColorInt())

            if (color == selectedColor)
                binding.ivSelectedColor.visibility = View.VISIBLE
            else
                binding.ivSelectedColor.visibility = View.GONE

            binding.root.setOnClickListener {
                onClick(color)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ColorListViewHolder {
        val binding = ItemLabelColorBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
        return ColorListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ColorListViewHolder, position: Int) {
        holder.bind(colors[position])
    }

    override fun getItemCount(): Int = colors.size
}