package com.ipb.simpt.ui.register

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.ipb.simpt.MainActivity
import com.ipb.simpt.databinding.ActivityRegisterBinding
import com.ipb.simpt.utils.Extensions.toast
import com.ipb.simpt.utils.FirebaseUtils.firebaseUser

class RegisterActivity : AppCompatActivity() {

    // view binding
    private lateinit var binding: ActivityRegisterBinding

    // firebase auth
    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var userName: String
    private lateinit var userNim: String
    private lateinit var userEmail: String
    private lateinit var userPassword: String
    private lateinit var createAccountInputsArray: Array<EditText>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // init firebase auth
        firebaseAuth = FirebaseAuth.getInstance()

        // input hint error for empty box
        createAccountInputsArray = arrayOf(
            binding.edRegisterName,
            binding.edRegisterNim,
            binding.edRegisterEmail,
            binding.edRegisterPassword,
            binding.edRegisterConfirmPassword
        )

        binding.btnRegister.setOnClickListener {
            setupAction()
        }
    }

    // check if there's a signed-in user
    override fun onStart() {
        super.onStart()
        val user: FirebaseUser? = firebaseAuth.currentUser
        user?.let {
            startActivity(Intent(this, MainActivity::class.java))
            toast("Welcome Back")
        }
    }

    // check if there's empty box
    private fun notEmpty(): Boolean = binding.edRegisterName.text.toString().trim().isNotEmpty() &&
            binding.edRegisterNim.text.toString().trim().isNotEmpty() &&
            binding.edRegisterEmail.text.toString().trim().isNotEmpty() &&
            binding.edRegisterPassword.text.toString().trim().isNotEmpty() &&
            binding.edRegisterConfirmPassword.text.toString().trim().isNotEmpty()

    private fun validateData(): Boolean {
        var valid = false
        if (notEmpty() &&
            binding.edRegisterPassword.text.toString()
                .trim() == binding.edRegisterConfirmPassword.text.toString().trim()
        ) {
            valid = true
        } else if (!notEmpty()) {
            createAccountInputsArray.forEach { input ->
                if (input.text.toString().trim().isEmpty()) {
                    input.error = "${input.hint} is required"
                }
            }
        } else {
            toast("Passwords are not matching !")
        }
        return valid
    }

    private fun setupAction() {
        // validateData() returns true only  when inputs are not empty and passwords are identical
        if (validateData()) {

            // input data
            userName = binding.edRegisterName.text.toString().trim()
            userNim = binding.edRegisterNim.text.toString().trim()
            userEmail = binding.edRegisterEmail.text.toString().trim()
            userPassword = binding.edRegisterPassword.text.toString().trim()

            // Show loading progress bar
            showLoading(true)

            // create account - firebase auth
            firebaseAuth.createUserWithEmailAndPassword(userEmail, userPassword)
                .addOnSuccessListener {
                    // account created, add user info to db
                    updateUserInfo()
                }
                .addOnFailureListener { e ->
                    // failed creating account
                    toast("Failed creating account due to ${e.message}")
                    // Hide loading progress bar
                    showLoading(false)
                }

        }

    }

    // Save user info - firebase firestore
    private fun updateUserInfo() {
        // timestamp
        val timestamp = System.currentTimeMillis()

        // get current user uid
        val uid = firebaseAuth.uid

        // setup data to add in db
        val hashMap: HashMap<String, Any?> = HashMap()
        hashMap["uid"] = uid
        hashMap["email"] = userEmail
        hashMap["userName"] = userName
        hashMap["userNim"] = userNim
        hashMap["profileImage"] = "" // empty. will do in profile edit
        hashMap["userType"] =
            "user" // possible value are user/dosen/admin. Will change value manually on firebase db
        hashMap["timestamp"] = timestamp

        //set data to db
        val db = FirebaseFirestore.getInstance()
        db.collection("Users")
            .document(uid!!)
            .set(hashMap)
            .addOnSuccessListener {
                // user info saved, open user main activity
                showLoading(false)
                toast("Account created successfully !")
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
            .addOnFailureListener { e ->
                // failed adding data to db
                showLoading(false)
                toast("Failed saving user info due to ${e.message}")
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