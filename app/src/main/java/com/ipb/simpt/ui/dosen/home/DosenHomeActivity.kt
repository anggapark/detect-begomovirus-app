package com.ipb.simpt.ui.dosen.home

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.ipb.simpt.R
import com.ipb.simpt.databinding.ActivityDosenHomeBinding
import com.ipb.simpt.ui.dosen.add.AddCategoryActivity
import com.ipb.simpt.ui.auth.splash.WelcomeActivity
import com.ipb.simpt.ui.dosen.approval.ApprovalActivity
import com.ipb.simpt.ui.dosen.library.DosenCategoryActivity
import com.ipb.simpt.utils.Extensions.toast

class DosenHomeActivity : AppCompatActivity() {

    // view binding
    private lateinit var binding: ActivityDosenHomeBinding

    // toolbar
    private lateinit var toolbar: Toolbar

    // firebase auth
    private lateinit var firebaseAuth: FirebaseAuth

    //TAG
    companion object {
        private const val TAG = "DosenHomeActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDosenHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupToolbar()

        // init firebase auth
        firebaseAuth = FirebaseAuth.getInstance()
        checkUser()

        setupAction()
    }
    //TODO: Bikin Profile Page


    private fun setupAction() {
        // handle button
        binding.cvCategory.setOnClickListener {
            startActivity(Intent(this, AddCategoryActivity::class.java))
        }
        binding.cvShow.setOnClickListener {
            startActivity(Intent(this, DosenCategoryActivity::class.java))
        }
        binding.cvApproval.setOnClickListener {
            startActivity(Intent(this, ApprovalActivity::class.java))
        }
    }

    //Authentication
    private fun checkUser() {
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser == null) {
            // Not signed in, launch the Welcome activity
            startActivity(Intent(this, WelcomeActivity::class.java))
            finish()
        }
        else {
            // logged in, get and show user profile
            val db = FirebaseFirestore.getInstance()
            db.collection("Users")
                .document(firebaseUser.uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        // get user info
                        val name = document.getString("userName")
                        val nim = document.getString("userNim")

                        // set to textview
                        binding.tvName.text = name
                        binding.tvNim.text = nim
                    } else {
                        Log.d(TAG, "No such document")
                    }
                }
                .addOnFailureListener { exception ->
                    Log.d(TAG, "get failed with ", exception)
                }

        }
    }

    // enable setupToolbar as actionbar
    private fun setupToolbar() {
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
    }

    // inflate menu to toolbar
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_profile, menu)
        return super.onCreateOptionsMenu(menu)
    }

    // handle sign out button
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_logout -> {
                signOut()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    // sign out from current user
    private fun signOut() {
        firebaseAuth.signOut()
        startActivity(Intent(this, WelcomeActivity::class.java))
        toast("signed out")
        finish()
    }
}