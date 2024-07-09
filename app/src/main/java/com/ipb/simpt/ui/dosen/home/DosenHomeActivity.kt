package com.ipb.simpt.ui.dosen.home

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
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

    // view binding
    private lateinit var binding: ActivityDosenHomeBinding

    // ViewModel
    private lateinit var profileViewModel: ProfileViewModel

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

        firebaseAuth = FirebaseAuth.getInstance()
        profileViewModel = ViewModelProvider(this).get(ProfileViewModel::class.java)

        checkUser()
        setupAction()
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
            supportFragmentManager.beginTransaction()
                .replace(binding.fragmentContainer.id, ProfileFragment())
                .addToBackStack(null)
                .commit()
        }
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
}