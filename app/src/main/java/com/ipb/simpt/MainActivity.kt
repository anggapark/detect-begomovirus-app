package com.ipb.simpt

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.ipb.simpt.databinding.ActivityMainBinding
import com.ipb.simpt.ui.auth.splash.WelcomeActivity

class MainActivity : AppCompatActivity() {

    // view binding & nav controller
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    // firebase auth
    private lateinit var firebaseAuth: FirebaseAuth

    //TAG
    companion object {
        private const val TAG = "USER_SHOW_TAG"
    }

    //TODO: REWORKING ON LAYOUT (
    // ALL FRAGMENT;
    // WELCOME AND LOGIN REGISTER;
    // APPROVAL)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // init firebase auth
        firebaseAuth = FirebaseAuth.getInstance()
        checkUser()

        //Navbar
        navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home,
                R.id.navigation_scan,
                R.id.navigation_library,
                R.id.navigation_profile
            )
        )
        binding.navView.setupWithNavController(navController)
    }

    // Authentication
    private fun checkUser() {
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser == null) {
            // Not signed in, launch the Welcome activity
            startActivity(Intent(this, WelcomeActivity::class.java))
            finish()
        } else {
            // Logged in, check user type
            val db = FirebaseFirestore.getInstance()
            db.collection("Users")
                .document(firebaseUser.uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val userType = document.getString("userType")
                        if (userType != "user") {
                            // Not a regular user, launch the Welcome activity
                            startActivity(Intent(this, WelcomeActivity::class.java))
                            finish()
                        }
                    } else {
                        Log.d(TAG, "No such document")
                    }
                }
                .addOnFailureListener { exception ->
                    Log.d(TAG, "get failed with ", exception)
                }
        }
    }

}