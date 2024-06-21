package com.ipb.simpt.ui.dosen.library.detail

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.ipb.simpt.R
import com.ipb.simpt.databinding.ActivityDosenCategoryDetailBinding

class DosenCategoryDetailActivity : AppCompatActivity() {

    // view binding
    private lateinit var binding: ActivityDosenCategoryDetailBinding
    private lateinit var toolbar: Toolbar

    private lateinit var itemId: String
    private lateinit var itemName: String
    private lateinit var categoryName: String
    private lateinit var categoryValue: String
    private var fragment: DosenCategoryRecyclerViewFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDosenCategoryDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupToolbar()

        // Get data from intent
        itemId = intent.getStringExtra("ITEM_ID") ?: ""
        itemName = intent.getStringExtra("ITEM_NAME") ?: ""
        categoryName = intent.getStringExtra("CATEGORY_NAME") ?: ""
        categoryValue = intent.getStringExtra("CATEGORY_VALUE") ?: ""

        loadFragments()
    }

    private fun loadFragments() {
        val fragmentTransaction = supportFragmentManager.beginTransaction()

        // Initialize your fragments
        val categoryFragment =
            DosenCategoryFragment.newInstance(itemId, itemName, categoryName, categoryValue)
        fragment = DosenCategoryRecyclerViewFragment.newInstance(
            itemId,
            itemName,
            categoryName,
            categoryValue
        )

        // Replace the fragments in your layout
        fragmentTransaction.replace(R.id.categoryFragmentContainer, categoryFragment)
        fragmentTransaction.replace(R.id.recyclerFragmentContainer, fragment!!)

        fragmentTransaction.commit()
    }

    // enable setupToolbar as actionbar
    private fun setupToolbar() {
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

//    override fun onCreateOptionsMenu(menu: Menu): Boolean {
//        menuInflater.inflate(R.menu.default_menu, menu)
//        return true
//    }

    // handle back button
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressedDispatcher.onBackPressed()
                return true
            }
//            R.id.action_refresh -> {
//                // Reload fragments
//                fragment?.fetchData()
//                return true
//            }
        }
        return super.onOptionsItemSelected(item)
    }
}