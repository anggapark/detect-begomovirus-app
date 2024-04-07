package com.ipb.simpt.ui.login

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseUser
import com.ipb.simpt.MainActivity
import com.ipb.simpt.R
import com.ipb.simpt.databinding.ActivityLoginBinding
import com.ipb.simpt.utils.Extensions.toast
import com.ipb.simpt.utils.FirebaseUtils
import com.ipb.simpt.utils.FirebaseUtils.firebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    lateinit var signInEmail: String
    lateinit var signInPassword: String
    lateinit var signInInputsArray: Array<EditText>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        signInInputsArray = arrayOf(binding.edLoginEmail, binding.edLoginPassword)
        binding.btnLogin.setOnClickListener {
            signIn()
        }
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

    private fun notEmpty(): Boolean = signInEmail.isNotEmpty() && signInPassword.isNotEmpty()

    private fun signIn() {
        signInEmail = binding.edLoginEmail.text.toString().trim()
        signInPassword = binding.edLoginPassword.text.toString().trim()


        if (notEmpty()) {
            firebaseAuth.signInWithEmailAndPassword(signInEmail, signInPassword)
                .addOnCompleteListener { signIn ->
                    if (signIn.isSuccessful) {
                        startActivity(Intent(this, MainActivity::class.java))
                        toast("signed in successfully")
                        finish()
                    } else {
                        toast("sign in failed")
                    }
                }
        } else {
            signInInputsArray.forEach { input ->
                if (input.text.toString().trim().isEmpty()) {
                    input.error = "${input.hint} is required"
                }
            }
        }
    }
}