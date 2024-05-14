package com.ipb.simpt.ui.splash

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowInsets
import android.view.WindowManager
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.ipb.simpt.MainActivity
import com.ipb.simpt.databinding.ActivityWelcomeBinding
import com.ipb.simpt.ui.home.DosenHomeActivity
import com.ipb.simpt.ui.login.LoginActivity
import com.ipb.simpt.ui.register.RegisterActivity
import com.ipb.simpt.utils.Extensions.toast

class WelcomeActivity : AppCompatActivity() {

    // view binding
    private lateinit var binding: ActivityWelcomeBinding

    // firebase auth
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWelcomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        enableEdgeToEdge()
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) window.insetsController?.hide(
            WindowInsets.Type.statusBars()
        )
        else window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        supportActionBar?.hide()


        // init firebase auth
        firebaseAuth = FirebaseAuth.getInstance()
        checkUser()

        setupAction()
    }

    private fun checkUser() {
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser !== null) {
            // User logged in, check user type
            val db = FirebaseFirestore.getInstance()
            db.collection("Users")
                .document(firebaseUser.uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        // get user type eg. user, dosen, admin
                        val userType = document.getString("userType")
                        if (userType == "user") {
                            startActivity(Intent(this@WelcomeActivity, MainActivity::class.java))
                            finish()

                        } else if (userType == "dosen") {
                            startActivity(Intent(this@WelcomeActivity, DosenHomeActivity::class.java))
                            finish()

                        } else if (userType == "admin") {
                            startActivity(Intent(this@WelcomeActivity, MainActivity::class.java))
                            finish()

                        }
                    } else {
                        toast("No such document")
                    }
                }
                .addOnFailureListener { e ->
                    toast("Failed to get document due to ${e.message}")
                }
        }
    }

    // TODO: Clean check user code
    // check if there's a signed-in user
//    override fun onStart() {
//        super.onStart()
//        val user: FirebaseUser? = FirebaseUtils.firebaseAuth.currentUser
//        user?.let {
//            startActivity(Intent(this, MainActivity::class.java))
//            toast("Welcome Back")
//        }
//    }

    private fun setupAction() {
        binding.loginButton.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        binding.signupButton.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        // TODO : Add continue without login
//        might add later for continue with login
//        binding.skipButton.setOnClickListener {
//
//        }
    }
}