package com.ipb.simpt.ui.dosen.approval

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.ipb.simpt.R
import com.ipb.simpt.databinding.ActivityApprovalBinding
import com.ipb.simpt.model.DataModel
import com.ipb.simpt.ui.adapter.ApprovalAdapter

class ApprovalActivity : AppCompatActivity() {

    // view binding
    private lateinit var binding: ActivityApprovalBinding

    // firebase auth
    private lateinit var firebaseAuth: FirebaseAuth

    // arraylist to hold data model
    private lateinit var dataArrayList: ArrayList<DataModel>
    private lateinit var viewModel: ApprovalViewModel
    private lateinit var adapter: ApprovalAdapter

    // Maintain the layout type state
    private var isGridLayout = false

    // Maintain the search query
    private var currentSearchQuery: String = ""


    //TAG
    companion object {
        private const val TAG = "ApprovalActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityApprovalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Auth and Firestore
        firebaseAuth = FirebaseAuth.getInstance()

        // Setup ViewModel
        viewModel = ViewModelProvider(this).get(ApprovalViewModel::class.java)

        // Setup Tabs
        setupTabs()

        // Setup Recycler View
        setupRecyclerView()

        // Setup toolbar
        setupToolbar()

        // Load initial data
        viewModel.fetchItemsByStatus("Pending")
        Log.d(TAG, "ApprovalActivity created and fetchItems called")
    }

    private fun setupTabs() {
        binding.tlCategory.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.let {
                    val selectedStatus = it.text.toString()
                    dataArrayList.clear() // Clear existing data
                    viewModel.fetchItemsByStatus(selectedStatus)
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun setupRecyclerView() {
        // init arraylist
        showLoading(true)  // Show progress bar
        dataArrayList = ArrayList()

        adapter = ApprovalAdapter(this, dataArrayList, viewModel) { dataModel ->
            val intent = Intent(this, ApprovalDetailActivity::class.java).apply {
                putExtra("ITEM_ID", dataModel.id)
            }
            startActivity(intent)
        }

        binding.rvData.adapter = adapter
        binding.rvData.layoutManager = LinearLayoutManager(this)

        // Observe data changes
        viewModel.items.observe(this, Observer { items ->
            if (items != null) {
                dataArrayList.addAll(items)
                adapter.updateData(dataArrayList)
                applySearchFilter()
                showLoading(false)
            } else {
                Log.d(TAG, "No items found")
                showLoading(false)
            }
        })
    }

    private fun applySearchFilter() {
        adapter.filter.filter(currentSearchQuery)
    }

    // enable setupToolbar as actionbar
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Approval"
    }

    // TODO: Search yg lebih advanced. by komoditas, by penyakit, by deskripsi, by user

    private fun toggleLayout() {
        isGridLayout = !isGridLayout
        applyLayoutManager()
        adapter.setLayoutType(isGridLayout)
        updateMenuIcon()
    }

    private fun applyLayoutManager() {
        binding.rvData.layoutManager = if (isGridLayout) {
            GridLayoutManager(this, 2)
        } else {
            LinearLayoutManager(this)
        }
    }

    private fun updateMenuIcon() {
        val menuItem = binding.toolbar.menu.findItem(R.id.action_layout)
        menuItem.icon = if (!isGridLayout) {
            AppCompatResources.getDrawable(this, R.drawable.ic_list_view)  // Icon for list layout
        } else {
            AppCompatResources.getDrawable(this, R.drawable.ic_grid_view)  // Icon for grid layout
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_approval, menu)
        val searchItem = menu?.findItem(R.id.action_search)
        val searchView = searchItem?.actionView as SearchView

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                currentSearchQuery = query ?: ""
                applySearchFilter()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                currentSearchQuery = newText ?: ""
                applySearchFilter()
                return true
            }
        })

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressedDispatcher.onBackPressed()
                return true
            }

            R.id.action_layout -> {
                toggleLayout()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }
}