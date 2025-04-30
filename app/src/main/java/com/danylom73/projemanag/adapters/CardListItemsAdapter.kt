package com.danylom73.projemanag.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.toColorInt
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.danylom73.projemanag.TaskListActivity
import com.danylom73.projemanag.databinding.ItemCardBinding
import com.danylom73.projemanag.models.Card
import com.danylom73.projemanag.models.SelectedMember
import com.google.android.play.integrity.internal.ac

class CardListItemsAdapter(
    private val activity: AppCompatActivity,
    private val items: ArrayList<Card>,
    private val parentPosition: Int,
    private val onClick: (Int, Int) -> Unit
) : RecyclerView.Adapter<CardListItemsAdapter.CardListViewHolder>() {

    inner class CardListViewHolder(private val binding: ItemCardBinding)
        : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Card) {
            binding.root.setOnClickListener {
                val childPosition = adapterPosition
                Log.d("CHILD_POS", childPosition.toString())
                onClick(parentPosition, childPosition)
            }

            if (item.labelColor.isNotEmpty()) {
                binding.viewLabelColor.visibility = View.VISIBLE
                binding.viewLabelColor.setBackgroundColor(item.labelColor.toColorInt())
            } else {
                binding.viewLabelColor.visibility = View.VISIBLE
            }

            binding.tvCardName.text = item.name

            if ((activity as TaskListActivity).assignedMembers.isNotEmpty()) {
                val selectedMembers = ArrayList<SelectedMember>()

                for (i in activity.assignedMembers.indices) {
                    for(j in item.assignedTo) {
                        if (activity.assignedMembers[i].id == j) {
                            val selectedMember = SelectedMember(
                                activity.assignedMembers[i].id,
                                activity.assignedMembers[i].image
                            )
                            selectedMembers.add(selectedMember)
                        }
                    }
                }

                if (selectedMembers.isNotEmpty()) {
                    if (selectedMembers.size == 1 &&
                        selectedMembers[0].id == item.createdBy) {
                        binding.rvCardSelectedMembersList.visibility = View.GONE
                    } else {
                        binding.rvCardSelectedMembersList.visibility = View.VISIBLE

                        binding.rvCardSelectedMembersList.layoutManager =
                            GridLayoutManager(activity, 4)
                        binding.rvCardSelectedMembersList.adapter =
                            CardMemberListItemsAdapter(activity, selectedMembers, false) {}
                    }
                } else {
                    binding.rvCardSelectedMembersList.visibility = View.GONE
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardListViewHolder {
        val binding = ItemCardBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
        return CardListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CardListViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size
}