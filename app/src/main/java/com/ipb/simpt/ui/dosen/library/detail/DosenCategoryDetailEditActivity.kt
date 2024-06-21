package com.ipb.simpt.ui.dosen.library.detail

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.ipb.simpt.R
import com.ipb.simpt.databinding.ActivityDosenCategoryDetailEditBinding
import com.ipb.simpt.ui.dosen.library.DosenCategoryListActivity
import com.ipb.simpt.ui.dosen.library.DosenCategoryViewModel

class DosenCategoryDetailEditActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDosenCategoryDetailEditBinding
    private lateinit var toolbar: Toolbar
    private lateinit var viewModel: DosenCategoryViewModel

    private lateinit var itemId: String
    private lateinit var itemName: String
    private lateinit var categoryName: String
    private lateinit var categoryValue: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDosenCategoryDetailEditBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupToolbar()

        viewModel = ViewModelProvider(this).get(DosenCategoryViewModel::class.java)

        // Get data from intent
        itemId = intent.getStringExtra("ITEM_ID") ?: ""
        itemName = intent.getStringExtra("ITEM_NAME") ?: ""
        categoryName = intent.getStringExtra("CATEGORY_NAME") ?: ""
        categoryValue = intent.getStringExtra("CATEGORY_VALUE") ?: ""

        binding.editText.setText(itemName)

        // Handle save button click
        binding.buttonSave.setOnClickListener {
            val updatedItemName = binding.editText.text.toString()
            if (updatedItemName.isNotBlank()) {
                viewModel.updateCategoryName(categoryName, categoryValue, itemId, updatedItemName)

                viewModel.updateResult.observe(this) { result ->
                    result.onSuccess {
                        Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
                        navigateToCategoryList()
                    }.onFailure {
                        Toast.makeText(this, it.localizedMessage, Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "Category name cannot be empty", Toast.LENGTH_SHORT).show()
            }
        }

    }

    //TODO: Intent to Previous Activity, not Jump to List
    private fun navigateToCategoryList() {
        val intent = Intent(this, DosenCategoryListActivity::class.java).apply {
            putExtra("CATEGORY_NAME", categoryName)
            putExtra("CATEGORY_VALUE", categoryValue)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(intent)
        finish()
    }

    // enable setupToolbar as actionbar
    private fun setupToolbar() {
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    // handle back button
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