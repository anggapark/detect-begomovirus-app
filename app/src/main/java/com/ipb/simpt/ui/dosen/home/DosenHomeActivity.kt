package com.ipb.simpt.ui.dosen.home

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.ipb.simpt.R
import com.ipb.simpt.databinding.ActivityDosenHomeBinding
import com.ipb.simpt.ui.auth.splash.WelcomeActivity
import com.ipb.simpt.ui.dosen.add.AddCategoryActivity
import com.ipb.simpt.ui.dosen.approval.ApprovalActivity
import com.ipb.simpt.ui.dosen.library.DosenCategoryActivity
import com.ipb.simpt.ui.mahasiswa.profile.ProfileFragment
import com.ipb.simpt.ui.mahasiswa.profile.ProfileViewModel
import com.ipb.simpt.utils.Extensions.toast

class DosenHomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDosenHomeBinding
    private lateinit var profileViewModel: ProfileViewModel
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var navController: NavController
    private var doubleBackToExitPressedOnce = false

    //TAG
    companion object {
        private const val TAG = "DosenHomeActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDosenHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        profileViewModel = ViewModelProvider(this).get(ProfileViewModel::class.java)

        setupNavController()
        checkUser()
        setupAction()
    }

    private fun setupNavController() {
        // Check if the NavHostFragment is already added
        var navHostFragment = supportFragmentManager
            .findFragmentById(R.id.fragment_container) as? NavHostFragment

        if (navHostFragment == null) {
            // Create NavHostFragment programmatically
            navHostFragment = NavHostFragment.create(R.navigation.dosen_navigation)
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, navHostFragment)
                .setPrimaryNavigationFragment(navHostFragment)
                .commitNow()


            // Hide the fragment container initially
            binding.fragmentContainer.visibility = View.GONE
        }

        navController = navHostFragment.navController
    }

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
        binding.cvProfile.setOnClickListener {
            if (!::navController.isInitialized) {
                setupNavController()
            }
            // Toggle visibility of fragment_container
            binding.fragmentContainer.visibility = View.VISIBLE
            navController.navigate(R.id.profileFragment)        }
    }

    //Authentication
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
                        setupUserProfile()
                        if (userType != "dosen") {
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

    private fun setupUserProfile() {
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser != null) {
            profileViewModel.fetchUserDetails(firebaseUser.uid)
            profileViewModel.user.observe(this) { user ->
                if (user != null) {
                    binding.tvName.text = user.userName
                    binding.tvNim.text = user.userNim
                    Glide.with(this)
                        .load(user.profileImage)
                        .placeholder(R.drawable.ic_profile) // Placeholder icon
                        .transform(CircleCrop())
                        .into(binding.ivProfile)
                }
            }
        }
    }

    override fun onBackPressed() {
        // Check if there's a fragment in the container
        if (binding.fragmentContainer.visibility == View.VISIBLE) {
            // Hide fragment container and navigate up in navController
            binding.fragmentContainer.visibility = View.GONE
            navController.navigateUp()
        } else {
            if (doubleBackToExitPressedOnce) {
                super.onBackPressed()
                return
            }

            doubleBackToExitPressedOnce = true
            Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show()

            Handler(Looper.getMainLooper()).postDelayed({
                doubleBackToExitPressedOnce = false
            }, 2000)
        }
    }
}