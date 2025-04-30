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
import com.bumptech.glide.Glide
import com.danylom73.projemanag.databinding.ActivityProfileBinding
import com.danylom73.projemanag.firebase.FireStoreClass
import com.danylom73.projemanag.models.User
import com.google.firebase.storage.FirebaseStorage

class ProfileActivity : BaseActivity() {
    private lateinit var binding: ActivityProfileBinding
    private lateinit var userDetails: User

    private var selectedImageUri: Uri? = null
    private var profileImageURL: String = ""

    private val pickImage = registerForActivityResult(
        ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        uri?.let {
            selectedImageUri = uri
            binding.ivProfileUserImage.setImageURI(uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        FireStoreClass().loadUserData(this)

        setupActionBar(
            binding.toolbarMyProfileActivity,
            resources.getString(R.string.profile_nav_title)
        )

        binding.ivProfileUserImage.setOnClickListener {
            pickImage.launch(arrayOf("image/*"))
        }

        binding.btnUpdate.setOnClickListener {
            if (selectedImageUri != null) {
                uploadUserImage()
            } else {
                showProgressDialog(resources.getString(R.string.please_wait))
                updateUserProfileData()
            }
        }
    }

    fun setUserDataInFields(user: User) {
        userDetails = user
        Glide
            .with(this@ProfileActivity)
            .load(user.image)
            .centerCrop()
            .placeholder(R.drawable.ic_user_place_holder)
            .into(binding.ivProfileUserImage)

        binding.etName.setText(user.name)
        binding.etEmail.setText(user.email)
        if (user.mobile != 0L)
            binding.etMobile.setText(user.mobile.toString())
    }

    private fun updateUserProfileData() {
        val userHashMap = HashMap<String, Any>()

        if (profileImageURL.isNotEmpty() &&
            profileImageURL != userDetails.image) {
            userHashMap["image"] = profileImageURL
        }

        if (binding.etName.text.toString().isNotEmpty() &&
            binding.etName.text.toString() != userDetails.name) {
            userHashMap["name"] = binding.etName.text.toString()
        }

        if (binding.etMobile.text.toString().isNotEmpty() &&
            binding.etMobile.text.toString() != userDetails.mobile.toString()) {
            userHashMap["mobile"] = binding.etMobile.text.toString().toLong()
        }

        FireStoreClass().updateUserData(this, userHashMap)
    }

    private fun uploadUserImage() {
        showProgressDialog(resources.getString(R.string.please_wait))
        if (selectedImageUri != null) {
            val ref = FirebaseStorage.getInstance().reference.child(
                "USER_IMAGE" + System.currentTimeMillis()
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
                            profileImageURL = uri.toString()
                            updateUserProfileData()
                            hideProgressDialog()
                        }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(
                        this@ProfileActivity,
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

    fun profileUpdateSuccess() {
        hideProgressDialog()
        setResult(RESULT_OK)
        finish()
    }
}