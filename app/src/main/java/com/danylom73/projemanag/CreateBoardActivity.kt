package com.danylom73.projemanag

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.danylom73.projemanag.databinding.ActivityCreateBoardBinding
import com.danylom73.projemanag.firebase.FireStoreClass
import com.danylom73.projemanag.models.Board
import com.danylom73.projemanag.utils.Constants
import com.google.firebase.storage.FirebaseStorage

class CreateBoardActivity : BaseActivity() {
    private lateinit var binding: ActivityCreateBoardBinding
    private lateinit var userName: String

    private var selectedImageUri: Uri? = null
    private var boardImageURL: String = ""

    private val pickImage = registerForActivityResult(
        ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        uri?.let {
            selectedImageUri = uri
            binding.ivBoardImage.setImageURI(uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateBoardBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        if (intent.hasExtra(Constants.NAME)) {
            userName = intent.getStringExtra(Constants.NAME)!!
        }

        setupActionBar(
            binding.toolbarCreateBoardActivity,
            resources.getString(R.string.create_board_title)
        )

        binding.ivBoardImage.setOnClickListener {
            pickImage.launch(arrayOf("image/*"))
        }

        binding.btnCreate.setOnClickListener {
            if (selectedImageUri != null) {
                uploadBoardImage()
            } else {
                showProgressDialog(resources.getString(R.string.please_wait))
                createBoard()
            }
        }
    }

    private fun createBoard() {
        val assignedUsers = ArrayList<String>()
        val currentUser = FireStoreClass().getCurrentUserID()
        assignedUsers.add(currentUser)

        var board = Board(
            name = binding.etBoardName.text.toString(),
            image = boardImageURL,
            createdBy = userName,
            assignedTo = assignedUsers
        )

        FireStoreClass().createBoard(this, board)
    }

    private fun uploadBoardImage() {
        showProgressDialog(resources.getString(R.string.please_wait))

        if (selectedImageUri != null) {
            val ref = FirebaseStorage.getInstance().reference.child(
                "BOARD_IMAGE" + System.currentTimeMillis()
                        + "." + getImageExtension(selectedImageUri)
            )
            ref.putFile(selectedImageUri!!)
                .addOnSuccessListener { taskSnapshot ->
                    Log.d(
                        "IMAGE_URL",
                        taskSnapshot.metadata!!.reference!!.downloadUrl.toString()
                    )
                    taskSnapshot.metadata!!.reference!!.downloadUrl
                        .addOnSuccessListener { uri ->
                            Log.i("IMAGE_URL", uri.toString())
                            boardImageURL = uri.toString()
                            createBoard()
                            hideProgressDialog()
                        }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(
                        this@CreateBoardActivity,
                        exception.message,
                        Toast.LENGTH_LONG
                    ).show()

                    hideProgressDialog()
                }
        }
    }

    private fun getImageExtension(uri: Uri?): String? {
        return MimeTypeMap.getSingleton()
            .getExtensionFromMimeType(contentResolver.getType(uri!!))
    }

    fun boardCreatedSuccessfully() {
        hideProgressDialog()
        setResult(RESULT_OK)
        finish()
    }
}