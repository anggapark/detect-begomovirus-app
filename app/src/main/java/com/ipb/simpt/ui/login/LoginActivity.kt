package com.ipb.simpt.ui.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.ipb.simpt.MainActivity
import com.ipb.simpt.databinding.ActivityLoginBinding
import com.ipb.simpt.ui.home.DosenHomeActivity
import com.ipb.simpt.utils.Extensions.toast
import com.ipb.simpt.utils.FirebaseUtils

class LoginActivity : AppCompatActivity() {

    // view binding
    private lateinit var binding: ActivityLoginBinding

    // firebase auth
    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var signInEmail: String
    private lateinit var signInPassword: String
    private lateinit var signInInputsArray: Array<EditText>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // init firebase auth
        firebaseAuth = FirebaseAuth.getInstance()

        // input hint error for empty box
        signInInputsArray = arrayOf(binding.edLoginEmail, binding.edLoginPassword)

        signIn()
    }

    // check if there's a signed-in user
    override fun onStart() {
        super.onStart()
        val user: FirebaseUser? = FirebaseUtils.firebaseAuth.currentUser
        user?.let {
            startActivity(Intent(this, MainActivity::class.java))
            toast("Welcome Back")
        }
    }

    // check if there's empty box
    private fun notEmpty(): Boolean = signInEmail.isNotEmpty() && signInPassword.isNotEmpty()

    private fun signIn() {
        binding.btnLogin.setOnClickListener {

            // input data
            signInEmail = binding.edLoginEmail.text.toString().trim()
            signInPassword = binding.edLoginPassword.text.toString().trim()

            // Show loading progress bar
            showLoading(true)

            if (notEmpty()) {
                firebaseAuth.signInWithEmailAndPassword(signInEmail, signInPassword)
                    .addOnSuccessListener {
                        // login success
                        checkUser()
                    }
                    .addOnFailureListener { e ->
                        // login failed
                        toast("Failed failed account due to ${e.message}")
                        // Hide loading progress bar
                        showLoading(false)
                    }
//                    .addOnCompleteListener { signIn ->
//                        if (signIn.isSuccessful) {
//                            startActivity(Intent(this, MainActivity::class.java))
//                            toast("signed in successfully")
//                            finish()
//                        } else {
//                            toast("sign in failed")
//                        }
//                    }
            } else {
                showLoading(false)
                signInInputsArray.forEach { input ->
                    if (input.text.toString().trim().isEmpty()) {
                        input.error = "${input.hint} is required"
                    }
                }
            }
        }
    }

    // Check user type - firebase Auth
    private fun checkUser() {
        // If user = Move to User Main Activity
        // If dosen = Move to Dosen Dashboard
        // If admin = Move to Admin Dashboard

        val firebaseUser = firebaseAuth.currentUser!!

        val db = FirebaseFirestore.getInstance()
        db.collection("Users")
            .document(firebaseUser.uid)
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    showLoading(false)

                    // get user type eg. user, dosen, admin
                    val userType = document.getString("userType")
                    if (userType == "user") {
                        startActivity(Intent(this@LoginActivity, MainActivity::class.java))

                    } else if (userType == "dosen") {
                        startActivity(Intent(this@LoginActivity, DosenHomeActivity::class.java))

                    } else if (userType == "admin") {
                        startActivity(Intent(this@LoginActivity, MainActivity::class.java))

                    }
                } else {
                    toast("No such document")
                }
            }
            .addOnFailureListener { e ->
                toast("Failed to get document due to ${e.message}")
            }
    }


    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }
}