package com.ipb.simpt.ui.mahasiswa.mydata

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.ipb.simpt.R
import com.ipb.simpt.databinding.ActivityMyDataBinding
import com.ipb.simpt.model.DataModel
import com.ipb.simpt.ui.adapter.MyDataAdapter
import com.ipb.simpt.ui.dosen.approval.ApprovalDetailActivity
import com.ipb.simpt.ui.mahasiswa.library.LibraryViewModel

class MyDataActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMyDataBinding
    private lateinit var toolbar: Toolbar

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var dataList: ArrayList<DataModel>
    private lateinit var viewModel: LibraryViewModel
    private lateinit var adapter: MyDataAdapter
    private var isGridLayout: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyDataBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupToolbar()

        firebaseAuth = FirebaseAuth.getInstance()
        viewModel = ViewModelProvider(this).get(LibraryViewModel::class.java)

        setupRecyclerView()
        setupLayoutSwitcher()
        setupSearchFilter()

    }

    private fun setupRecyclerView() {
        showLoading(true)
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser != null) {
            viewModel.fetchItemsByUserId(firebaseUser.uid)
        }

        dataList = ArrayList()

        adapter = MyDataAdapter(this, dataList, viewModel) { dataModel ->
            val intent = Intent(this, ApprovalDetailActivity::class.java).apply {
                putExtra("ITEM_ID", dataModel.id)
                putExtra("FROM_MY_DATA", true)
            }
            startActivity(intent)
        }

        binding.rvLibrary.adapter = adapter
        binding.rvLibrary.layoutManager = LinearLayoutManager(this) // Default to list layout

        // Observe data changes
        viewModel.items.observe(this, Observer { items ->
            if (items != null) {
                dataList.clear()
                dataList.addAll(items)
                adapter.updateData(dataList)
                showLoading(false)
            } else {
                showLoading(false)
            }
        })
    }

    private fun setupLayoutSwitcher() {
        binding.btnList.setOnClickListener {
            isGridLayout = false
            applyLayoutManager()
            adapter.setLayoutType(isGridLayout)
            updateLayoutSwitcherBackground()
        }

        binding.btnGrid.setOnClickListener {
            isGridLayout = true
            applyLayoutManager()
            adapter.setLayoutType(isGridLayout)
            updateLayoutSwitcherBackground()
        }
    }

    private fun updateLayoutSwitcherBackground() {
        if (isGridLayout) {
            binding.btnList.setBackgroundResource(R.drawable.bg_text)
            binding.btnGrid.setBackgroundResource(R.drawable.bg_text_selected)
        } else {
            binding.btnList.setBackgroundResource(R.drawable.bg_text_selected)
            binding.btnGrid.setBackgroundResource(R.drawable.bg_text)
        }
    }

    private fun applyLayoutManager() {
        binding.rvLibrary.layoutManager = if (isGridLayout) {
            GridLayoutManager(this, 2)
        } else {
            LinearLayoutManager(this)
        }
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

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun setupToolbar() {
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setTitle(R.string.home_data)
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