package com.ipb.simpt.ui.auth.splash

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
import com.ipb.simpt.ui.admin.AdminHomeActivity
import com.ipb.simpt.ui.dosen.home.DosenHomeActivity
import com.ipb.simpt.ui.auth.login.LoginActivity
import com.ipb.simpt.ui.auth.register.RegisterActivity
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
                        val intent = when (userType) {
                            "user" -> Intent(this@WelcomeActivity, MainActivity::class.java)
                            "dosen" -> Intent(this@WelcomeActivity, DosenHomeActivity::class.java)
                            "admin" -> Intent(this@WelcomeActivity, AdminHomeActivity::class.java)
                            else -> null
                        }
                        intent?.let {
                            it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(it)
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

    // TODO: Waktu authentikasi berapa lama sebelum otomatis logout

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