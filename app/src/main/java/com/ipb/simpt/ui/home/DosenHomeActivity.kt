package com.ipb.simpt.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.firebase.auth.FirebaseAuth
import com.ipb.simpt.R
import com.ipb.simpt.databinding.ActivityDosenHomeBinding
import com.ipb.simpt.ui.add.AddCategoryActivity
import com.ipb.simpt.ui.library.DosenShowActivity
import com.ipb.simpt.ui.splash.WelcomeActivity
import com.ipb.simpt.utils.Extensions.toast

class DosenHomeActivity : AppCompatActivity() {

    // view binding
    private lateinit var binding: ActivityDosenHomeBinding

    // toolbar
    private lateinit var toolbar: Toolbar

    // firebase auth
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDosenHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()
        toolbar()

        // init firebase auth
        firebaseAuth = FirebaseAuth.getInstance()
        checkUser()

        // handle button
        binding.cvCategory.setOnClickListener {
            startActivity(Intent(this, AddCategoryActivity::class.java))
        }
        binding.cvShow.setOnClickListener {
            startActivity(Intent(this, DosenShowActivity::class.java))
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
    }

    // enable toolbar as actionbar
    private fun toolbar() {
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