package com.danylom73.projemanag.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.danylom73.projemanag.R
import com.danylom73.projemanag.databinding.ItemBoardBinding
import com.danylom73.projemanag.models.Board

class BoardListAdapter(
    private val activity: AppCompatActivity,
    private val boards: ArrayList<Board>,
    private val onClick: (Board) -> Unit
) : RecyclerView.Adapter<BoardListAdapter.BoardListViewHolder>() {

    inner class BoardListViewHolder(private val binding: ItemBoardBinding)
        : RecyclerView.ViewHolder(binding.root) {

        fun bind(board: Board) {
            binding.root.setOnClickListener { onClick(board) }
            binding.tvName.text = board.name
            binding.tvCreatedBy.text = activity.getString(
                R.string.created_by, board.createdBy
            )
            Glide
                .with(activity)
                .load(board.image)
                .centerCrop()
                .placeholder(R.drawable.ic_board_place_holder)
                .into(binding.ivBoardImage)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BoardListViewHolder {
        val binding = ItemBoardBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
        return BoardListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BoardListViewHolder, position: Int) {
        holder.bind(boards[position])
    }

    override fun getItemCount(): Int = boards.size
}