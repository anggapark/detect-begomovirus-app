package com.ipb.simpt.ui.dosen.library.detail

import android.os.Bundle
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.ipb.simpt.R
import com.ipb.simpt.databinding.ActivityDosenCategoryDetailBinding

class DosenCategoryDetailActivity : AppCompatActivity() {

    // view binding
    private lateinit var binding: ActivityDosenCategoryDetailBinding

    // toolbar
    private lateinit var toolbar: Toolbar

    private lateinit var itemId: String
    private lateinit var itemName: String
    private lateinit var categoryName: String
    private lateinit var categoryValue: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDosenCategoryDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()
        setupToolbar()

        // Get data from intent
        itemId = intent.getStringExtra("ITEM_ID") ?: ""
        itemName = intent.getStringExtra("ITEM_NAME") ?: ""
        categoryName = intent.getStringExtra("CATEGORY_NAME") ?: ""
        categoryValue = intent.getStringExtra("CATEGORY_VALUE") ?: ""

        // Load the DosenCategoryFragment
        supportFragmentManager.beginTransaction()
            .replace(R.id.categoryFragmentContainer, DosenCategoryFragment.newInstance(itemId, itemName, categoryName, categoryValue))
            .commit()

        // Load the DosenCategoryRecyclerViewFragment
        supportFragmentManager.beginTransaction()
            .replace(R.id.recyclerFragmentContainer, DosenCategoryRecyclerViewFragment.newInstance(itemId, itemName, categoryName, categoryValue))
            .commit()
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

    fun refreshRecyclerView() {
        val fragment = supportFragmentManager.findFragmentById(R.id.recyclerFragmentContainer)
        if (fragment is DosenCategoryRecyclerViewFragment) {
            fragment.fetchData()
        }
    }


}