package com.danylom73.projemanag

import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.danylom73.projemanag.adapters.MemberListItemAdapter
import com.danylom73.projemanag.databinding.ActivityMembersBinding
import com.danylom73.projemanag.firebase.FireStoreClass
import com.danylom73.projemanag.models.Board
import com.danylom73.projemanag.models.User
import com.danylom73.projemanag.utils.Constants
import com.danylom73.projemanag.utils.NotificationHelper

class MembersActivity : BaseActivity() {
    private lateinit var binding: ActivityMembersBinding
    private lateinit var boardDetails: Board
    private lateinit var assignedMembers: ArrayList<User>

    private var anyChangeMade: Boolean = false

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMembersBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (anyChangeMade) {
                    setResult(RESULT_OK)
                }
                finish()
            }
        }

        onBackPressedDispatcher.addCallback(this, callback)

        if (intent.hasExtra(Constants.BOARD_DETAIL)) {
            boardDetails = intent.getParcelableExtra(
                Constants.BOARD_DETAIL, Board::class.java
            )!!
        }

        setupActionBar(
            binding.toolbarMembersActivity,
            resources.getString(R.string.members)
        )

        showProgressDialog(resources.getString(R.string.please_wait))
        FireStoreClass().getAssignedMembers(this, boardDetails.assignedTo)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_add_member, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        showAddMemberDialog()
        return super.onOptionsItemSelected(item)
    }

    fun setupMembersList(list: ArrayList<User>) {
        assignedMembers = list
        hideProgressDialog()

        binding.rvMembersList.layoutManager = LinearLayoutManager(this)
        binding.rvMembersList.setHasFixedSize(true)
        binding.rvMembersList.adapter = MemberListItemAdapter(this, list) {_, _, _ ->}
    }

    private fun showAddMemberDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_search_member)
        dialog.findViewById<TextView>(R.id.tv_add).setOnClickListener {
            val email = dialog.findViewById<EditText>(R.id.et_email_search_member)
                .text.toString()

            if (email.isNotEmpty()) {
                dialog.dismiss()
                showProgressDialog(resources.getString(R.string.please_wait))
                FireStoreClass().getMemberDetails(this, email)
            } else {
                Toast.makeText(
                    this@MembersActivity,
                    "Please enter member's email address",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
        dialog.findViewById<TextView>(R.id.tv_cancel).setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    fun getMemberDetails(user: User) {
        boardDetails.assignedTo.add(user.id)
        FireStoreClass().assignMemberToBoard(this, boardDetails, user)
    }

    fun memberAssignSuccess(user: User) {
        hideProgressDialog()
        assignedMembers.add(user)
        anyChangeMade = true
        setupMembersList(assignedMembers)
        NotificationHelper().sendNotification(
            user.id,
            "New Board assign",
            " You have been assigned to a new board '${boardDetails.name}'"
        )
    }
}