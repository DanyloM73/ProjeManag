package com.danylom73.projemanag.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.danylom73.projemanag.R
import com.danylom73.projemanag.databinding.ItemCardSelectedMemberBinding
import com.danylom73.projemanag.models.SelectedMember

class CardMemberListItemsAdapter(
    private val activity: AppCompatActivity,
    private val selectedMembers: ArrayList<SelectedMember>,
    private val assignMembers: Boolean = true,
    private val onClick: () -> Unit
) : RecyclerView.Adapter<CardMemberListItemsAdapter.CardMemberListViewHolder>() {

    inner class CardMemberListViewHolder(private val binding: ItemCardSelectedMemberBinding)
        : RecyclerView.ViewHolder(binding.root) {

        fun bind(member: SelectedMember, position: Int) {
            if (position == selectedMembers.size - 1 && assignMembers) {
                binding.ivAddMember.visibility = View.VISIBLE
                binding.ivSelectedMemberImage.visibility = View.GONE
            } else {
                binding.ivAddMember.visibility = View.GONE
                binding.ivSelectedMemberImage.visibility = View.VISIBLE
                Glide
                    .with(activity)
                    .load(member.image)
                    .centerCrop()
                    .placeholder(R.drawable.ic_user_place_holder)
                    .into(binding.ivSelectedMemberImage)
            }

            binding.root.setOnClickListener {
                onClick()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardMemberListViewHolder {
        val binding = ItemCardSelectedMemberBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
        return CardMemberListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CardMemberListViewHolder, position: Int) {
        holder.bind(selectedMembers[position], position)
    }

    override fun getItemCount(): Int = selectedMembers.size
}