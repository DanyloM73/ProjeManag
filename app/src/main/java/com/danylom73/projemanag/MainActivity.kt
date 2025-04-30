package com.danylom73.projemanag

import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.danylom73.projemanag.adapters.BoardListAdapter
import com.danylom73.projemanag.databinding.ActivityMainBinding
import com.danylom73.projemanag.databinding.MainContentBinding
import com.danylom73.projemanag.databinding.NavHeaderMainBinding
import com.danylom73.projemanag.firebase.FireStoreClass
import com.danylom73.projemanag.models.Board
import com.danylom73.projemanag.models.User
import com.danylom73.projemanag.utils.Constants
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import androidx.core.content.edit
import com.danylom73.projemanag.R
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navHeaderBinding: NavHeaderMainBinding
    private lateinit var mainContentBinding: MainContentBinding
    private lateinit var userName: String
    private lateinit var sharedPreferences: SharedPreferences

    private val profileActivityLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            FireStoreClass().loadUserData(this)
        } else {
            Log.e("LAUNCH_PROFILE", "Canceled")
        }
    }

    private val boardActivityLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            FireStoreClass().getBoardList(this)
        } else {
            Log.e("LAUNCH_BOARD", "Canceled")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        FireStoreClass().loadUserData(this, true)

        sharedPreferences = getSharedPreferences(Constants.PREFERENCES, MODE_PRIVATE)
        val tokenUpdated = sharedPreferences.getBoolean(Constants.TOKEN_UPDATED, false)

        if (tokenUpdated) {
            showProgressDialog(resources.getString(R.string.please_wait))
            FireStoreClass().loadUserData(this, true)
        } else {
            FirebaseMessaging.getInstance().token
                .addOnCompleteListener { task ->
                    updateFCMToken(task.result)
                }
        }

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                } else {
                    doubleBackToExit()
                }
            }
        }

        onBackPressedDispatcher.addCallback(this, callback)

        binding.navView.setNavigationItemSelectedListener(this)

        val headerView = binding.navView.getHeaderView(0)
        navHeaderBinding = NavHeaderMainBinding.bind(headerView)

        mainContentBinding = binding.appBarMain.mainContent

        setupActionBar(
            toolbar = binding.appBarMain.toolbarMainActivity,
            navigationIconResId = R.drawable.ic_action_navigation_menu,
            onNavigationClick = { toggleDrawer() }
        )

        binding.appBarMain.fabCreateBoard.setOnClickListener {
            val intent = Intent(this, CreateBoardActivity::class.java)
            intent.putExtra(Constants.NAME, userName)
            boardActivityLauncher.launch(intent)
        }
    }

    private fun toggleDrawer() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_my_profile -> {
                profileActivityLauncher
                    .launch(Intent(this, ProfileActivity::class.java))
            }
            R.id.nav_sign_out -> {
                FirebaseAuth.getInstance().signOut()
                sharedPreferences.edit { clear() }
                val intent = Intent(this, IntroActivity::class.java)
                intent.addFlags(
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_NEW_TASK
                )
                startActivity(intent)
                finish()
            }
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    fun updateNavigationUserDetails(user: User, readBoardList: Boolean) {
        hideProgressDialog()
        userName = user.name

        Glide
            .with(this)
            .load(user.image)
            .centerCrop()
            .placeholder(R.drawable.ic_user_place_holder)
            .into(navHeaderBinding.ivUserImage)

        navHeaderBinding.tvUsername.text = user.name

        if (readBoardList) {
            showProgressDialog(resources.getString(R.string.please_wait))
            FireStoreClass().getBoardList(this)
        }
    }

    fun populateBoardList(boardList: ArrayList<Board>) {
        hideProgressDialog()

        if (boardList.isNotEmpty()) {
            mainContentBinding.rvBoardsList.visibility = View.VISIBLE
            mainContentBinding.tvNoBoards.visibility = View.GONE

            mainContentBinding.rvBoardsList
                .layoutManager = LinearLayoutManager(this)
            mainContentBinding.rvBoardsList.setHasFixedSize(true)

            val adapter = BoardListAdapter(this, boardList) { board ->
                val intent = Intent(this, TaskListActivity::class.java)
                intent.putExtra(Constants.DOCUMENT_ID, board.documentId)
                startActivity(intent)
            }
            mainContentBinding.rvBoardsList.adapter = adapter
        } else {
            mainContentBinding.rvBoardsList.visibility = View.GONE
            mainContentBinding.tvNoBoards.visibility = View.VISIBLE
        }
    }

    fun tokenUpdateSuccess() {
        hideProgressDialog()
        sharedPreferences.edit {
            putBoolean(Constants.TOKEN_UPDATED, true)
        }
        showProgressDialog(resources.getString(R.string.please_wait))
        FireStoreClass().loadUserData(this, true)
    }

    private fun updateFCMToken(token: String) {
        val userHashMap = HashMap<String, Any>()
        userHashMap[Constants.FCM_TOKEN] = token
        showProgressDialog(resources.getString(R.string.please_wait))
        FireStoreClass().updateUserData(this, userHashMap)
    }
}