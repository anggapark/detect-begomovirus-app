package com.ipb.simpt.ui.mahasiswa.library

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.ipb.simpt.R
import com.ipb.simpt.databinding.ActivityApprovalDetailBinding
import com.ipb.simpt.databinding.ActivityLibraryDetailBinding
import com.ipb.simpt.model.DataModel
import com.ipb.simpt.ui.dosen.approval.ApprovalDetailViewModel

class LibraryDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLibraryDetailBinding
    private lateinit var viewModel: LibraryViewModel
    private lateinit var itemId: String

    //TODO: Go to User Detail on Click
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLibraryDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize ViewModel
        viewModel = ViewModelProvider(this).get(LibraryViewModel::class.java)

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
        binding.tvUploaderName.text = data.userName
        binding.tvUploaderNim.text = data.userNim

        // Load image
        Glide.with(this).load(data.url).into(binding.ivImage)

        // Hide loading indicator after data is fetched and UI is updated
        showLoading(false)

    }

    private fun setupAction(itemId: String) {
        binding.llUploader.setOnClickListener {

        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Data Detail"
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