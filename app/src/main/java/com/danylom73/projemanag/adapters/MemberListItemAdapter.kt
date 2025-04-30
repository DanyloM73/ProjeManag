package com.danylom73.projemanag.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.danylom73.projemanag.R
import com.danylom73.projemanag.databinding.ItemMemberBinding
import com.danylom73.projemanag.models.User
import com.danylom73.projemanag.utils.Constants

class MemberListItemAdapter(
    private val activity: AppCompatActivity,
    private val members: ArrayList<User>,
    private val onClick: (Int, User, String) -> Unit
) : RecyclerView.Adapter<MemberListItemAdapter.MemberListViewHolder>() {

    inner class MemberListViewHolder(private val binding: ItemMemberBinding)
        : RecyclerView.ViewHolder(binding.root) {

        fun bind(member: User, position: Int) {
            binding.root.setOnClickListener {
                var onClickAction =
                    if (member.selected) Constants.UNSELECT else Constants.SELECT
                onClick(position, member, onClickAction)
            }
            binding.tvMemberName.text = member.name
            binding.tvMemberEmail.text = member.email
            Glide
                .with(activity)
                .load(member.image)
                .centerCrop()
                .placeholder(R.drawable.ic_board_place_holder)
                .into(binding.ivMemberImage)

            if (member.selected) {
                binding.ivSelectedMember.visibility = View.VISIBLE
            } else {
                binding.ivSelectedMember.visibility = View.GONE
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemberListViewHolder {
        val binding = ItemMemberBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
        return MemberListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MemberListViewHolder, position: Int) {
        holder.bind(members[position], position)
    }

    override fun getItemCount(): Int = members.size
}