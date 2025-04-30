package com.danylom73.projemanag

import android.app.DatePickerDialog
import android.icu.util.Calendar
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.toColorInt
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.danylom73.projemanag.adapters.CardMemberListItemsAdapter
import com.danylom73.projemanag.databinding.ActivityCardDetailsBinding
import com.danylom73.projemanag.dialogs.LabelColorListDialog
import com.danylom73.projemanag.dialogs.MembersListDialog
import com.danylom73.projemanag.firebase.FireStoreClass
import com.danylom73.projemanag.models.Board
import com.danylom73.projemanag.models.Card
import com.danylom73.projemanag.models.SelectedMember
import com.danylom73.projemanag.models.User
import com.danylom73.projemanag.utils.Constants
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CardDetailsActivity : BaseActivity() {
    private lateinit var binding: ActivityCardDetailsBinding
    private lateinit var boardDetails: Board
    private lateinit var members: ArrayList<User>

    private var cardDetails = Card()
    private var taskPosition = -1
    private var cardPosition = -1
    private var selectedColor = ""
    private var selectedDueDate: Long = 0

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCardDetailsBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        getIntentData()
        setupActionBar(
            binding.toolbarCardDetailsActivity,
            cardDetails.name
        )

        binding.etNameCardDetails.setText(cardDetails.name)

        selectedColor = cardDetails.labelColor
        if (selectedColor.isNotEmpty())
            setColor()

        binding.btnUpdateCardDetails.setOnClickListener {
            if (binding.etNameCardDetails.text.toString().isNotEmpty())
                updateCardDetails()
            else
                Toast.makeText(
                    this@CardDetailsActivity,
                    "Please enter Card name",
                    Toast.LENGTH_LONG
                ).show()
        }

        binding.tvSelectLabelColor.setOnClickListener {
            colorListDialog()
        }

        binding.tvSelectMembers.setOnClickListener {
            memberListDialog()
        }

        setupSelectedMembers()

        selectedDueDate = cardDetails.dueDate
        if (selectedDueDate > 0) {
            val simpleDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)
            binding.tvSelectDueDate.text =
                simpleDateFormat.format(Date(selectedDueDate))
        }

        binding.tvSelectDueDate.setOnClickListener {
            showDataPicker()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_delete_card, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        alertDialogForDeleteCard(cardDetails.name)
        return super.onOptionsItemSelected(item)
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun getIntentData() {
        boardDetails = intent.getParcelableExtra(
            Constants.BOARD_DETAIL, Board::class.java
        )!!
        taskPosition = intent.getIntExtra(Constants.TASK_POSITION, -1)
        cardPosition = intent.getIntExtra(Constants.CARD_POSITION, -1)
        cardDetails = boardDetails.taskList[taskPosition].cards[cardPosition]
        members = intent.getParcelableArrayListExtra(
            Constants.BOARD_MEMBERS, User::class.java
        )!!
    }

    override fun addUpdateTaskListSuccess() {
        hideProgressDialog()
        setResult(RESULT_OK)
        finish()
    }

    private fun updateCardDetails() {
        val card = cardDetails.copy(
            name = binding.etNameCardDetails.text.toString(),
            labelColor = selectedColor,
            dueDate = selectedDueDate
        )

        boardDetails.taskList.removeAt(boardDetails.taskList.size - 1)

        boardDetails.taskList[taskPosition].cards[cardPosition] = card

        showProgressDialog(resources.getString(R.string.please_wait))
        FireStoreClass().addUpdateTaskList(this@CardDetailsActivity, boardDetails)
    }

    private fun alertDialogForDeleteCard(cardName: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(resources.getString(R.string.alert))
        builder.setMessage(
            resources.getString(
                R.string.confirm_message,
                cardName
            )
        )
        builder.setIcon(android.R.drawable.ic_dialog_alert)
        builder.setPositiveButton("YES") { dialogInterface, which ->
            dialogInterface.dismiss()
            deleteCard()
        }
        builder.setNegativeButton("NO") { dialogInterface, which ->
            dialogInterface.dismiss()
        }

        val alertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()
    }

    private fun deleteCard() {
        val cardList = boardDetails.taskList[taskPosition].cards
        cardList.removeAt(cardPosition)
        val taskList = boardDetails.taskList
        taskList.removeAt(taskList.size - 1)

        taskList[taskPosition].cards = cardList

        showProgressDialog(resources.getString(R.string.please_wait))
        FireStoreClass().addUpdateTaskList(this@CardDetailsActivity, boardDetails)
    }

    private fun setColor() {
        binding.tvSelectLabelColor.text = ""
        binding.tvSelectLabelColor.setBackgroundColor(selectedColor.toColorInt())
    }

    private fun colorListDialog() {
        val colorsDialog = LabelColorListDialog(
            this, Constants.colors, "Select color", selectedColor
        ) { color ->
            selectedColor = color
            setColor()
        }
        colorsDialog.show()
    }

    private fun memberListDialog() {
        var assignedMembers = cardDetails.assignedTo

        if (assignedMembers.isNotEmpty()) {
            for (i in members.indices) {
                for (j in assignedMembers) {
                    if (members[i].id == j) {
                        members[i].selected = true
                    }
                }
            }
        } else {
            for (i in members.indices) {
                members[i].selected = false
            }
        }

        val membersDialog = MembersListDialog(
            this, members, "Select Members"
        ) { user, action ->
            if (action == Constants.SELECT) {
                if (!cardDetails.assignedTo.contains(user.id)) {
                    cardDetails.assignedTo.add(user.id)
                }
            } else {
                cardDetails.assignedTo.remove(user.id)

                for (i in members.indices) {
                    if (members[i].id == user.id) {
                        members[i].selected = false
                    }
                }
            }

            setupSelectedMembers()
        }
        membersDialog.show()
    }

    private fun setupSelectedMembers() {
        val assignedMembers = cardDetails.assignedTo
        val selectedMembers = ArrayList<SelectedMember>()

        for (i in members.indices) {
            for (j in assignedMembers) {
                if (members[i].id == j) {
                    val selectedMember = SelectedMember(
                        members[i].id,
                        members[i].image
                    )
                    selectedMembers.add(selectedMember)
                }
            }
        }

        if (selectedMembers.isNotEmpty()) {
            selectedMembers.add(SelectedMember("", ""))
            binding.tvSelectMembers.visibility = View.GONE
            binding.rvSelectedMembersList.visibility = View.VISIBLE

            binding.rvSelectedMembersList.layoutManager = GridLayoutManager(
                this, 6
            )
            binding.rvSelectedMembersList.adapter = CardMemberListItemsAdapter(
                this, selectedMembers
            ) {
                memberListDialog()
            }
        } else {
            binding.tvSelectMembers.visibility = View.VISIBLE
            binding.rvSelectedMembersList.visibility = View.GONE
        }
    }

    private fun showDataPicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                val formatDay = if (dayOfMonth < 10) "0$dayOfMonth" else "$dayOfMonth"
                val formatMonth = if ((monthOfYear + 1) < 10) "0${monthOfYear + 1}" else "${monthOfYear + 1}"
                val selectedDate = "$formatDay/$formatMonth/$year"
                binding.tvSelectDueDate.text = selectedDate
                val simpleDateFormat = SimpleDateFormat(
                    "dd/MM/yyyy", Locale.ENGLISH
                )
                val date = simpleDateFormat.parse(selectedDate)
                if (date != null) {
                    selectedDueDate = date.time
                }
            },
            year, month, day
        )
        datePickerDialog.show()
    }
}