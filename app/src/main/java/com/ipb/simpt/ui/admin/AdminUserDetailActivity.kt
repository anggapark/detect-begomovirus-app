package com.ipb.simpt.ui.admin

import android.os.Bundle
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.ipb.simpt.R
import com.ipb.simpt.databinding.ActivityAdminHomeBinding
import com.ipb.simpt.databinding.ActivityAdminUserDetailBinding
import com.ipb.simpt.ui.dosen.approval.ApprovalBottomSheetFragment
import com.ipb.simpt.ui.dosen.approval.ApprovalViewModel
import com.ipb.simpt.ui.mahasiswa.profile.ProfileViewModel

class AdminUserDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminUserDetailBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var viewModel: ProfileViewModel
    private lateinit var userId: String
    private lateinit var toolbar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminUserDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupToolbar()
        firebaseAuth = FirebaseAuth.getInstance()
        userId = intent.getStringExtra("USER_ID") ?: ""
        viewModel = ViewModelProvider(this).get(ProfileViewModel::class.java)

        setupUserProfile(userId)
        binding.tvUserChange.setOnClickListener {
            val bottomSheet = AdminBottomSheetFragment.newInstance(userId)
            bottomSheet.show(supportFragmentManager, "AdminBottomSheetFragment")
        }
    }

    private fun setupUserProfile(userId: String) {
        viewModel.fetchUserDetails(userId)
        viewModel.user.observe(this) { user ->
            if (user != null) {
                binding.tvName.text = user.userName
                binding.tvNim.text = user.userNim
                binding.tvEmail.text = user.email
                Glide.with(this)
                    .load(user.profileImage)
                    .placeholder(R.drawable.ic_profile) // Placeholder icon
                    .into(binding.ivProfile)
            }
        }
    }

    private fun setupToolbar() {
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "User Detail"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressedDispatcher.onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}