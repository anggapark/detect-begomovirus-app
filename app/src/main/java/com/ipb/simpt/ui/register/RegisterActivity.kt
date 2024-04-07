package com.ipb.simpt.ui.register

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseUser
import com.ipb.simpt.MainActivity
import com.ipb.simpt.databinding.ActivityRegisterBinding
import com.ipb.simpt.utils.Extensions.toast
import com.ipb.simpt.utils.FirebaseUtils.firebaseAuth
import com.ipb.simpt.utils.FirebaseUtils.firebaseUser

class RegisterActivity : AppCompatActivity() {

    // TODO 1: save user data such as name and nim
    // TODO 2: apply loading state

    lateinit var userName: String
    lateinit var userNim: String
    lateinit var userEmail: String
    lateinit var userPassword: String
    private lateinit var binding: ActivityRegisterBinding
    lateinit var createAccountInputsArray: Array<EditText>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        createAccountInputsArray = arrayOf(
            binding.edRegisterName,
            binding.edRegisterNim,
            binding.edRegisterEmail,
            binding.edRegisterPassword,
            binding.edRegisterConfirmPassword
        )

        setupAction()
    }

    /* check if there's a signed-in user*/
    override fun onStart() {
        super.onStart()
        val user: FirebaseUser? = firebaseAuth.currentUser
        user?.let {
            startActivity(Intent(this, MainActivity::class.java))
            toast("Welcome Back")
        }
    }

    private fun notEmpty(): Boolean = binding.edRegisterName.text.toString().trim().isNotEmpty() &&
            binding.edRegisterNim.text.toString().trim().isNotEmpty() &&
            binding.edRegisterEmail.text.toString().trim().isNotEmpty() &&
            binding.edRegisterPassword.text.toString().trim().isNotEmpty() &&
            binding.edRegisterConfirmPassword.text.toString().trim().isNotEmpty()

    private fun identicalPassword(): Boolean {
        var identical = false
        if (notEmpty() &&
            binding.edRegisterPassword.text.toString()
                .trim() == binding.edRegisterConfirmPassword.text.toString().trim()
        ) {
            identical = true
        } else if (!notEmpty()) {
            createAccountInputsArray.forEach { input ->
                if (input.text.toString().trim().isEmpty()) {
                    input.error = "${input.hint} is required"
                }
            }
        } else {
            toast("passwords are not matching !")
        }
        return identical
    }

    private fun setupAction() {
        binding.btnRegister.setOnClickListener {
            if (identicalPassword()) {
                // identicalPassword() returns true only  when inputs are not empty and passwords are identical

                userName = binding.edRegisterName.text.toString().trim()
                userNim = binding.edRegisterNim.text.toString().trim()
                userEmail = binding.edRegisterEmail.text.toString().trim()
                userPassword = binding.edRegisterPassword.text.toString().trim()

                /*create a user*/
                firebaseAuth.createUserWithEmailAndPassword(userEmail, userPassword)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            toast("created account successfully !")
                            // TODO : Implement email verification
                            //sendEmailVerification()
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                        } else {
                            toast("failed to Authenticate !")
                        }
                    }
            }
        }
    }

    /* send verification email to the new user. This will only
    *  work if the firebase user is not null.
    */

    private fun sendEmailVerification() {
        firebaseUser?.let {
            it.sendEmailVerification().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    toast("email sent to $userEmail")
                }
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }
}