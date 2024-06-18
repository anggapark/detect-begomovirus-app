package com.ipb.simpt.ui.dosen.library

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.ipb.simpt.R
import com.ipb.simpt.databinding.ActivityDosenCategoryListBinding
import com.ipb.simpt.model.CategoryModel
import com.ipb.simpt.ui.adapter.CategoryAdapter

class DosenCategoryListActivity : AppCompatActivity() {

    // view binding
    private lateinit var binding: ActivityDosenCategoryListBinding

    // toolbar
    private lateinit var toolbar: Toolbar

    // arraylist to hold data model
    private lateinit var categoryList: ArrayList<CategoryModel>
    private lateinit var viewModel: DosenCategoryListViewModel
    private lateinit var adapter: CategoryAdapter

    private lateinit var categoryName: String
    private lateinit var categoryValue: String

    //TAG
    companion object {
        private const val TAG = "DosenCategoryListActivity"
        const val REQUEST_CODE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDosenCategoryListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupToolbar()

        // Get data from intent
        categoryName = intent.getStringExtra("CATEGORY_NAME") ?: ""
        categoryValue = intent.getStringExtra("CATEGORY_VALUE") ?: ""

        // Setup ViewModel
        viewModel = ViewModelProvider(this).get(DosenCategoryListViewModel::class.java)

        // Setup Recycler View
        setupRecyclerView()

        // Setup Search Filter
        setupSearchFilter()

        // Load initial data
        viewModel.fetchCategoryData(categoryName, categoryValue)
        Log.d(TAG, "DosenCategoryListActivity created and fetchItems called")
    }

    private fun setupRecyclerView() {
        showLoading(true)
        categoryList = ArrayList()

        adapter = CategoryAdapter(this, categoryList, categoryName, categoryValue)
        binding.rvCategories.adapter = adapter

        // Observe data changes
        viewModel.categoryData.observe(this, Observer { categoryData ->
            if (categoryData != null) {
                categoryList.clear()
                categoryList.addAll(categoryData)
                adapter.updateData(categoryList)
                showLoading(false)
            } else {
                Log.d(TAG, "No categoryData found")
                showLoading(false)
            }
        })
    }

    private fun setupSearchFilter() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
                // called as and when user type anything
                try {
                    adapter.filter.filter(s)
                } catch (e: Exception) {

                }
            }

            override fun afterTextChanged(p0: Editable?) {

            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            viewModel.fetchCategoryData(categoryName, categoryValue) // Refetch data
        }
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

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

}