package com.ipb.simpt.ui.dosen.approval

import android.os.Bundle
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.ipb.simpt.databinding.ActivityApprovalDetailBinding
import com.ipb.simpt.repository.DataRepository

class ApprovalDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityApprovalDetailBinding
    private lateinit var viewModel: ApprovalViewModel
    private val dataRepository = DataRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityApprovalDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()
        val itemId = intent.getStringExtra("itemId") ?: return

        // Setup toolbar
        setupToolbar()

        // Fetch item details
//        setupViewModel(itemId)

        // Handle click
        setupAction(itemId)
    }

    private fun setupAction(itemId: String) {

        binding.btnApprove.setOnClickListener {
            finish()
        }

        binding.btnReject.setOnClickListener {
            finish()
        }
    }


//    private fun setupViewModel(itemId: String) {
//        viewModel = ViewModelProvider(this).get(ApprovalViewModel::class.java)
//
//        viewModel.fetchItemById(itemId)
//        viewModel.item.observe(this, Observer { item ->
//            item?.let {
//                dataRepository.fetchNames(it) { updatedItem ->
//                    binding.tvKomoditas.text = updatedItem.komoditasId
//                    binding.tvPenyakit.text = updatedItem.penyakitId
//                    binding.tvKategoriPathogen.text = updatedItem.kategoriPathogen
//                    binding.tvPathogen.text = updatedItem.pathogenId
//                    binding.tvGejala.text = updatedItem.gejalaId
//                    binding.tvDataset.text = updatedItem.dataset
//                    binding.tvDeskripsi.text = updatedItem.deskripsi
//
//                    // Load image
//                    Glide.with(this)
//                        .load(updatedItem.url)
//                        .into(binding.ivImage)
//                }
//            }
//        })
//    }


    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
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
}