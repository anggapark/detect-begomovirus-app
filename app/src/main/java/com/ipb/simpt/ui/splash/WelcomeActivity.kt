package com.ipb.simpt.ui.splash

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowInsets
import android.view.WindowManager
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseUser
import com.ipb.simpt.MainActivity
import com.ipb.simpt.databinding.ActivityWelcomeBinding
import com.ipb.simpt.ui.login.LoginActivity
import com.ipb.simpt.ui.register.RegisterActivity
import com.ipb.simpt.utils.Extensions.toast
import com.ipb.simpt.utils.FirebaseUtils

class WelcomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWelcomeBinding

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

        setupAction()
    }

    /* check if there's a signed-in user*/
    override fun onStart() {
        super.onStart()
        val user: FirebaseUser? = FirebaseUtils.firebaseAuth.currentUser
        user?.let {
            startActivity(Intent(this, MainActivity::class.java))
            toast("Welcome Back")
        }
    }

    private fun setupAction() {
        binding.loginButton.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        binding.signupButton.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}