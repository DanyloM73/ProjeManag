package com.danylom73.projemanag

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.danylom73.projemanag.adapters.TaskListItemsAdapter
import com.danylom73.projemanag.databinding.ActivityTaskListBinding
import com.danylom73.projemanag.firebase.FireStoreClass
import com.danylom73.projemanag.models.Board
import com.danylom73.projemanag.models.Card
import com.danylom73.projemanag.models.Task
import com.danylom73.projemanag.models.User
import com.danylom73.projemanag.utils.Constants

class TaskListActivity : BaseActivity() {
    private lateinit var binding: ActivityTaskListBinding
    private lateinit var boardDetails: Board
    lateinit var assignedMembers: ArrayList<User>

    private var boardDocumentId = ""

    private val activityLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            showProgressDialog(resources.getString(R.string.please_wait))
            FireStoreClass().getBoardDetails(this, boardDocumentId)
        } else {
            Log.e("LAUNCH_MEMBERS", "Canceled")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTaskListBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        if (intent.hasExtra(Constants.DOCUMENT_ID)) {
            boardDocumentId = intent.getStringExtra(Constants.DOCUMENT_ID)!!
        }

        showProgressDialog(resources.getString(R.string.please_wait))
        FireStoreClass().getBoardDetails(this, boardDocumentId)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_members, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val intent = Intent(this, MembersActivity::class.java)
        intent.putExtra(Constants.BOARD_DETAIL, boardDetails)
        activityLauncher.launch(intent)
        return super.onOptionsItemSelected(item)
    }

    fun boardDetails(board: Board) {
        boardDetails = board

        hideProgressDialog()
        setupActionBar(
            binding.toolbarTaskListActivity,
            boardDetails.name
        )

        showProgressDialog(resources.getString(R.string.please_wait))
        FireStoreClass().getAssignedMembers(this, boardDetails.assignedTo)
    }

    override fun addUpdateTaskListSuccess() {
        hideProgressDialog()
        showProgressDialog(resources.getString(R.string.please_wait))
        FireStoreClass().getBoardDetails(this, boardDetails.documentId)
    }

    fun createTaskList(taskListName: String) {
        val task = Task(taskListName, FireStoreClass().getCurrentUserID())
        boardDetails.taskList.add(0, task)
        boardDetails.taskList.removeAt(boardDetails.taskList.size - 1)
        showProgressDialog(resources.getString(R.string.please_wait))
        FireStoreClass().addUpdateTaskList(this, boardDetails)
    }

    fun updateTaskList(position: Int, listName: String, item: Task) {
        val task = Task(listName, item.createdBy)
        boardDetails.taskList[position] = task
        boardDetails.taskList.removeAt(boardDetails.taskList.size - 1)
        showProgressDialog(resources.getString(R.string.please_wait))
        FireStoreClass().addUpdateTaskList(this, boardDetails)
    }

    fun deleteTaskList(position: Int) {
        boardDetails.taskList.removeAt(position)
        boardDetails.taskList.removeAt(boardDetails.taskList.size - 1)
        showProgressDialog(resources.getString(R.string.please_wait))
        FireStoreClass().addUpdateTaskList(this, boardDetails)
    }

    fun addCardToTaskList(position: Int, cardName: String) {
        boardDetails.taskList.removeAt(boardDetails.taskList.size - 1)

        val assignedUsers = ArrayList<String>()
        assignedUsers.add(FireStoreClass().getCurrentUserID())

        val card = Card(
            cardName,
            FireStoreClass().getCurrentUserID(),
            assignedUsers
        )

        val cardList = boardDetails.taskList[position].cards
        cardList.add(card)

        val task = Task(
            boardDetails.taskList[position].title,
            boardDetails.taskList[position].createdBy,
            cardList
        )

        boardDetails.taskList[position] = task

        showProgressDialog(resources.getString(R.string.please_wait))
        FireStoreClass().addUpdateTaskList(this, boardDetails)
    }

    fun cardDetails(taskListPosition: Int, cardPosition: Int) {
        val intent = Intent(this, CardDetailsActivity::class.java)
        intent.putExtra(Constants.BOARD_DETAIL, boardDetails)
        intent.putExtra(Constants.TASK_POSITION, taskListPosition)
        intent.putExtra(Constants.CARD_POSITION, cardPosition)
        intent.putExtra(Constants.BOARD_MEMBERS, assignedMembers)
        activityLauncher.launch(intent)
    }

    fun boardMembers(list: ArrayList<User>) {
        assignedMembers = list
        hideProgressDialog()

        val addTaskList = Task(resources.getString(R.string.add_list))
        boardDetails.taskList.add(addTaskList)

        binding.rvTaskList.layoutManager = LinearLayoutManager(
            this, LinearLayoutManager.HORIZONTAL, false
        )
        binding.rvTaskList.setHasFixedSize(true)
        binding.rvTaskList.adapter = TaskListItemsAdapter(
            this, boardDetails.taskList
        )
    }

    fun updateCardsInTaskList(
        listPosition: Int, cards: ArrayList<Card>
    ) {
        boardDetails.taskList.removeAt(boardDetails.taskList.size - 1)
        boardDetails.taskList[listPosition].cards = cards
        showProgressDialog(resources.getString(R.string.please_wait))
        FireStoreClass().addUpdateTaskList(this, boardDetails)
    }
}