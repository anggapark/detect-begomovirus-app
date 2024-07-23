package com.ipb.simpt.ui.mahasiswa.library

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.ipb.simpt.R
import com.ipb.simpt.databinding.ActivityLibraryDetailBinding
import com.ipb.simpt.model.DataModel
import com.ipb.simpt.ui.mahasiswa.add.AddDataBottomSheetFragment
import com.ipb.simpt.ui.mahasiswa.library.user.LibraryUserDetailActivity

class LibraryDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLibraryDetailBinding
    private lateinit var viewModel: LibraryViewModel
    private lateinit var data: DataModel
    private lateinit var itemId: String
    private var fromMyData: Boolean = false
    private var isActivityActive = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLibraryDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize ViewModel
        viewModel = ViewModelProvider(this).get(LibraryViewModel::class.java)

        // Get item ID from intent
        itemId = intent.getStringExtra("ITEM_ID") ?: ""
        fromMyData = intent.getBooleanExtra("FROM_MY_DATA", false)

        // Setup toolbar
        setupToolbar()

        // Show loading indicator
        showLoading(true)

        // Load item details
        loadItemDetails()

        // Handle click
        setupAction()
    }

    override fun onResume() {
        super.onResume()
        isActivityActive = true
    }

    override fun onPause() {
        super.onPause()
        isActivityActive = false
    }

    private fun loadItemDetails() {
        viewModel.fetchItemDetails(itemId) { dataModel ->
            viewModel.fetchAndCacheNames(dataModel) {
                data = dataModel
                if (isActivityActive) {
                    updateUI(dataModel)
                }
            }
        }
    }

    private fun updateUI(data: DataModel) {
        // Update UI elements with cached names
        if (!isActivityActive) return

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
                binding.tvName.text = user.userName
                binding.tvNim.text = user.userNim
                Glide.with(this)
                    .load(user.profileImage)
                    .placeholder(R.drawable.ic_profile) // Placeholder icon
                    .transform(CircleCrop())
                    .into(binding.ivProfile)
                showLoading(false)
            } else {
                showLoading(false)
            }
        })
    }

    private fun setupAction() {
        if (fromMyData) {
            binding.cvProfile.isClickable = false
        } else {
            binding.cvProfile.setOnClickListener {
                val intent = Intent(this, LibraryUserDetailActivity::class.java)
                intent.putExtra("USER_ID", data.uid)
                this.startActivity(intent)
            }
        }
        binding.btnAddDatasetImage.setOnClickListener {
            val nonNullItemId = itemId
            val bottomSheetFragment =
                AddDataBottomSheetFragment.newInstance(nonNullItemId, fromOtherActivity = true)
            bottomSheetFragment.show(supportFragmentManager, bottomSheetFragment.tag)

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
        setViewsEnabled(!isLoading)
    }

    private fun setViewsEnabled(isEnabled: Boolean) {
        binding.btnAddDatasetImage.isEnabled = isEnabled
        binding.toolbar.menu.setGroupEnabled(0, isEnabled)
    }
}