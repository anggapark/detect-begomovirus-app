package com.ipb.simpt.ui.dosen.approval

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.ipb.simpt.R
import com.ipb.simpt.databinding.ActivityApprovalDetailBinding
import com.ipb.simpt.model.DataModel

class ApprovalDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityApprovalDetailBinding
    private lateinit var viewModel: ApprovalViewModel
    private lateinit var itemId: String

    //TODO: ADD COMMENT WHEN REJECT
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityApprovalDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize ViewModel
        viewModel = ViewModelProvider(this).get(ApprovalViewModel::class.java)

        // Get item ID from intent
        itemId = intent.getStringExtra("ITEM_ID") ?: ""

        // Setup toolbar
        setupToolbar()

        // Show loading indicator
        showLoading(true)

        // Load item details
        loadItemDetails()

        // Handle click
        setupAction(itemId)
    }

    private fun loadItemDetails() {
        viewModel.fetchItemDetails(itemId) { dataModel ->
            // Fetch names using cached data
            viewModel.fetchAndCacheNames(dataModel) {
                updateUI(dataModel)
            }
        }
    }

    private fun updateUI(data: DataModel) {
        // Update UI elements with cached names

        binding.tvKomoditas.text = data.komoditasName
        binding.tvPenyakit.text = data.penyakitName
        binding.tvKategoriPathogen.text = data.kategoriPathogen
        binding.tvPathogen.text = data.pathogenName
        binding.tvGejala.text = data.gejalaName
        binding.tvDataset.text = data.dataset
        binding.tvDescription.text = data.deskripsi

        // Load image
        Glide.with(this).load(data.url).into(binding.ivImage)

        // load user details
        updateUserUI(data.uid)

        // Hide loading indicator after data is fetched and UI is updated
        showLoading(false)

    }

    private fun updateUserUI(uid: String) {
        viewModel.fetchUserDetails(uid)
        viewModel.user.observe(this, Observer { user ->
            if (user != null) {
                binding.tvUploaderName.text = user.userName
                binding.tvUploaderNim.text = user.userNim
                Glide.with(this)
                    .load(user.profileImage)
                    .placeholder(R.drawable.ic_profile) // Placeholder icon
                    .transform(CircleCrop())
                    .into(binding.ivUploader)
                showLoading(false)
            } else {
                showLoading(false)
            }
        })
    }


    // TODO: ADD COMMENT WHEN APPROVE OR REJECT
    private fun setupAction(itemId: String) {
        binding.btnApprove.setOnClickListener {
            viewModel.approveData(itemId)
            Toast.makeText(this, "Item Approved", Toast.LENGTH_SHORT).show()
            finish()
        }

        binding.btnReject.setOnClickListener {
            viewModel.rejectData(itemId)
            Toast.makeText(this, "Item Rejected", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Approval Detail"
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressedDispatcher.onBackPressed()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }
}