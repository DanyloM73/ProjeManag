package com.danylom73.projemanag

import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.danylom73.projemanag.databinding.ActivitySignUpBinding
import com.danylom73.projemanag.firebase.FireStoreClass
import com.danylom73.projemanag.models.User
import com.google.firebase.auth.FirebaseAuth

class SignUpActivity : BaseActivity() {
    private lateinit var binding: ActivitySignUpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        setupActionBar(
            toolbar = binding.toolbarSignUpActivity,
            navigationIconResId = R.drawable.ic_back
        )

        binding.btnSignUp.setOnClickListener {
            registerUser()
        }
    }

    private fun registerUser() {
        val name = binding.etName.text.toString().trim{ it <= ' ' }
        val email = binding.etEmail.text.toString().trim{ it <= ' ' }
        val password = binding.etPassword.text.toString().trim{ it <= ' ' }

        if (validateForm(name, email, password)) {
            showProgressDialog(resources.getString(R.string.registering_new_user))
            FirebaseAuth.getInstance()
                .createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    hideProgressDialog()
                    if (task.isSuccessful) {
                        val firebaseUser = task.result.user
                        val user = User(
                            firebaseUser!!.uid.toString(),
                            name,
                            firebaseUser.email.toString()
                        )
                        FireStoreClass().registerUser(this, user)
                    } else {
                        Toast.makeText(
                            this,
                            "Sign up failed",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
        }
    }

    private fun validateForm(
        name: String,
        email: String,
        password: String
    ): Boolean {
        return when {
            name.isEmpty() ||
            email.isEmpty() ||
            password.isEmpty() -> {
                showErrorSnackBar("Please fill in all fields")
                false
            }
            else -> true
        }
    }

    fun userRegisteredSuccess() {
        Toast.makeText(
            this,
            "Sign in successfully",
            Toast.LENGTH_LONG
        ).show()
        hideProgressDialog()
        FirebaseAuth.getInstance().signOut()
        finish()
    }
}