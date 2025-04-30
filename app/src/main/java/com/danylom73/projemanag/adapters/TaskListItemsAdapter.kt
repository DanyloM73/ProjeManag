package com.danylom73.projemanag.adapters

import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.danylom73.projemanag.TaskListActivity
import com.danylom73.projemanag.databinding.ItemTaskBinding
import com.danylom73.projemanag.models.Task
import java.util.Collections

class TaskListItemsAdapter(
    private val activity: AppCompatActivity,
    private val items: ArrayList<Task>
) : RecyclerView.Adapter<TaskListItemsAdapter.TaskListViewHolder>() {

    private var positionDraggedFrom = -1
    private var positionDraggedTo = -1

    inner class TaskListViewHolder(private val binding: ItemTaskBinding)
        : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Task, position: Int) {
            if (position == items.size - 1) {
                binding.tvAddTaskList.visibility = View.VISIBLE
                binding.llTaskItem.visibility = View.GONE
            } else {
                binding.tvAddTaskList.visibility = View.GONE
                binding.llTaskItem.visibility = View.VISIBLE
            }

            binding.tvTaskListTitle.text = item.title
            binding.tvAddTaskList.setOnClickListener {
                binding.tvAddTaskList.visibility = View.GONE
                binding.cvAddTaskListName.visibility = View.VISIBLE
            }

            binding.ibCloseListName.setOnClickListener {
                binding.tvAddTaskList.visibility = View.VISIBLE
                binding.cvAddTaskListName.visibility = View.GONE
            }

            binding.ibDoneListName.setOnClickListener {
                val listName = binding.etTaskListName.text.toString()

                if (listName.isNotEmpty()) {
                    if (activity is TaskListActivity) {
                        activity.createTaskList(listName)
                    }
                } else {
                    Toast.makeText(
                        activity,
                        "Please enter List name",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            binding.ibEditListName.setOnClickListener {
                binding.etEditTaskListName.setText(item.title)
                binding.llTitleView.visibility = View.GONE
                binding.cvEditTaskListName.visibility = View.VISIBLE
            }

            binding.ibCloseEditableView.setOnClickListener {
                binding.llTitleView.visibility = View.VISIBLE
                binding.cvEditTaskListName.visibility = View.GONE
            }

            binding.ibDoneEditListName.setOnClickListener {
                val listName = binding.etEditTaskListName.text.toString()

                if (listName.isNotEmpty()) {
                    if (activity is TaskListActivity) {
                        activity.updateTaskList(position, listName, item)
                    }
                } else {
                    Toast.makeText(
                        activity,
                        "Please enter new List name",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            binding.ibDeleteList.setOnClickListener {
                deleteAlertDialog(position, item.title)
            }

            binding.tvAddCard.setOnClickListener {
                binding.tvAddCard.visibility = View.GONE
                binding.cvAddCard.visibility = View.VISIBLE
            }

            binding.ibCloseCardName.setOnClickListener {
                binding.tvAddCard.visibility = View.VISIBLE
                binding.cvAddCard.visibility = View.GONE
            }

            binding.ibDoneCardName.setOnClickListener {
                val cardName = binding.etCardName.text.toString()

                if (cardName.isNotEmpty()) {
                    if (activity is TaskListActivity) {
                        activity.addCardToTaskList(position, cardName)
                    }
                } else {
                    Toast.makeText(
                        activity,
                        "Please enter Card name",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            binding.rvCardList.layoutManager = LinearLayoutManager(activity)
            binding.rvCardList.setHasFixedSize(true)
            binding.rvCardList.adapter = CardListItemsAdapter(
                activity, item.cards, position
            ) { parentPos, childPos ->
                if (activity is TaskListActivity) {
                    activity.cardDetails(parentPos, childPos)
                }
            }

            val dividerItemDecoration = DividerItemDecoration(
                activity, DividerItemDecoration.VERTICAL
            )
            binding.rvCardList.addItemDecoration(dividerItemDecoration)

            val helper = ItemTouchHelper(
                object : ItemTouchHelper.SimpleCallback(
                    ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0
                ) {
                    override fun onMove(
                        recyclerView: RecyclerView,
                        viewHolder: RecyclerView.ViewHolder,
                        target: RecyclerView.ViewHolder
                    ): Boolean {
                        val draggedPosition = viewHolder.adapterPosition
                        val targetPosition = target.adapterPosition

                        if (positionDraggedFrom == -1) {
                            positionDraggedFrom = draggedPosition
                        }
                        positionDraggedTo = targetPosition
                        Collections.swap(
                            items[position].cards, draggedPosition, targetPosition
                        )
                        notifyItemMoved(draggedPosition, targetPosition)
                        return false
                    }

                    override fun onSwiped(
                        viewHolder: RecyclerView.ViewHolder,
                        direction: Int
                    ) {}

                    override fun clearView(
                        recyclerView: RecyclerView,
                        viewHolder: RecyclerView.ViewHolder
                    ) {
                        super.clearView(recyclerView, viewHolder)
                        if(positionDraggedFrom != -1 && positionDraggedTo != -1 &&
                            positionDraggedTo != positionDraggedFrom) {
                            (activity as TaskListActivity).updateCardsInTaskList(
                                position, items[position].cards
                            )
                        }
                        positionDraggedTo = -1
                        positionDraggedFrom = -1
                    }
                }
            )

            helper.attachToRecyclerView(binding.rvCardList)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskListViewHolder {
        val binding = ItemTaskBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
        val layoutParams = LinearLayout.LayoutParams(
            (parent.width * 0.7).toInt(), LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins(15.toDp().toPx(), 0, 40.toDp().toPx(), 0)
        binding.root.layoutParams = layoutParams
        return TaskListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskListViewHolder, position: Int) {
        holder.bind(
            items[position],
            position
        )
    }

    override fun getItemCount(): Int = items.size

    private fun deleteAlertDialog(position: Int, title: String) {
        val builder = AlertDialog.Builder(activity)
        builder.setTitle("Deletion")
        builder.setMessage("Are you sure you want to delete $title")
        builder.setIcon(android.R.drawable.ic_dialog_alert)
        builder.setPositiveButton("Yes") { dialogInterface, which ->
            dialogInterface.dismiss()

            if (activity is TaskListActivity) {
                activity.deleteTaskList(position)
            }
        }
        builder.setNegativeButton("No") { dialogInterface, which ->
            dialogInterface.dismiss()
        }

        val alertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()
    }

    private fun Int.toDp(): Int =
        (this / Resources.getSystem().displayMetrics.density).toInt()

    private fun Int.toPx(): Int =
        (this * Resources.getSystem().displayMetrics.density).toInt()
}