package com.ipb.simpt.ui.dosen.approval

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.ipb.simpt.R
import com.ipb.simpt.databinding.ActivityApprovalDetailBinding
import com.ipb.simpt.model.DataModel
import com.ipb.simpt.ui.mahasiswa.add.AddDataActivity
import com.ipb.simpt.ui.mahasiswa.add.AddDataBottomSheetFragment

class ApprovalDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityApprovalDetailBinding
    private lateinit var viewModel: ApprovalViewModel
    private lateinit var itemId: String
    private lateinit var userType: String
    private var fromMyData: Boolean = false

    //TODO: Add UID of commentator
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityApprovalDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize ViewModel
        viewModel = ViewModelProvider(this).get(ApprovalViewModel::class.java)

        // Get item ID from intent
        itemId = intent.getStringExtra("ITEM_ID") ?: ""
        userType = intent.getStringExtra("USER_TYPE") ?: "dosen"
        fromMyData = intent.getBooleanExtra("FROM_MY_DATA", false)

        // Setup toolbar
        setupToolbar()

        // Show loading indicator
        showLoading(true)

        // Load item details
        loadItemDetails()

        // Handle click
        setupAction(itemId)

        // Handle fragment result
        getResult()
    }

    private fun loadItemDetails() {
        viewModel.fetchItemDetails(itemId) { dataModel ->
            viewModel.fetchAndCacheNames(dataModel) {
                updateUI(dataModel)
            }
        }
    }

    // Update UI elements with cached names
    private fun updateUI(data: DataModel) {
        // Load status
        binding.tvStatus.text = data.status
        when (data.status) {
            "Pending" -> binding.tvStatus.background =
                AppCompatResources.getDrawable(this, R.drawable.bg_pending)

            "Approved" -> binding.tvStatus.background =
                AppCompatResources.getDrawable(this, R.drawable.bg_approved)

            "Rejected" -> binding.tvStatus.background =
                AppCompatResources.getDrawable(this, R.drawable.bg_rejected)
        }

        // Load text view
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

    private fun setupAction(itemId: String) {
        if (fromMyData) {
            binding.tvResponse.visibility = View.GONE
            binding.tvResponse.isClickable = false
        } else {
            binding.tvResponse.text = if (userType == "user") {
                getString(R.string.title_response_user)
            } else {
                getString(R.string.title_response_dosen)
            }

            binding.tvResponse.setOnClickListener {
                val bottomSheet = ApprovalBottomSheetFragment.newInstance(itemId, userType ?: "")
                bottomSheet.show(supportFragmentManager, "ApprovalBottomSheetFragment")
            }
            setupEditButton()
        }
        binding.btnAddDatasetImage.setOnClickListener {
            val nonNullItemId = itemId
            val bottomSheetFragment =
                AddDataBottomSheetFragment.newInstance(nonNullItemId, fromOtherActivity = true)
            bottomSheetFragment.show(supportFragmentManager, bottomSheetFragment.tag)

        }
    }

    private fun setupEditButton() {
        if (userType == "user") {
            binding.btnEdit.visibility = View.VISIBLE
            binding.btnEdit.setOnClickListener {
                val intent = Intent(this, AddDataActivity::class.java).apply {
                    putExtra("ITEM_ID", itemId)
                    putExtra("USER_TYPE", userType)
                }
                startActivity(intent)
            }
        }
    }

    private fun getResult() {
        supportFragmentManager.setFragmentResultListener("approvalResult", this) { _, bundle ->
            val dataChanged = bundle.getBoolean("dataChanged")
            if (dataChanged) {
                val intent = Intent(this, ApprovalActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                startActivity(intent)
            }
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = when {
            fromMyData -> "Data Detail"
            userType == "user" -> "Submission Detail"
            else -> "Approval Detail"
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if (userType == "user") {
            menuInflater.inflate(R.menu.menu_delete, menu)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressedDispatcher.onBackPressed()
                true
            }

            R.id.action_delete -> {
                deleteItem()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun deleteItem() {
        AlertDialog.Builder(this)
            .setTitle("Confirm Deletion")
            .setMessage("Are you sure you want to delete this data?")
            .setPositiveButton("Yes") { dialog, _ ->
                viewModel.deleteData(itemId) { success ->
                    if (success) {
                        dialog.dismiss()
                        val intent = Intent(this, ApprovalActivity::class.java).apply {
                            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                            putExtra("USER_TYPE", userType)
                        }
                        startActivity(intent)
                    } else {
                        dialog.dismiss()
                        Toast.makeText(this, "Failed to delete item", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        setViewsEnabled(!isLoading)
    }

    private fun setViewsEnabled(isEnabled: Boolean) {
        binding.tvResponse.isEnabled = isEnabled
        binding.btnEdit.isEnabled = isEnabled
        binding.btnAddDatasetImage.isEnabled = isEnabled
        binding.toolbar.menu.setGroupEnabled(0, isEnabled)
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.onCleared() // Cancel ongoing operations
    }
}