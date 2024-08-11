package com.ipb.simpt.ui.dosen.approval

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
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

    // userType to distinguish between dosen and user
    private var userType: String? = null

    //TAG
    companion object {
        private const val TAG = "ApprovalActivity"
        private const val DETAIL_REQUEST_CODE = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityApprovalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Auth and Firestore
        firebaseAuth = FirebaseAuth.getInstance()

        // Retrieve userType from intent
        userType = intent.getStringExtra("USER_TYPE") ?: "dosen"

        // Setup ViewModel
        viewModel = ViewModelProvider(this).get(ApprovalViewModel::class.java)

        // Setup Tabs
        setupTabs()

        // Setup Recycler View
        setupRecyclerView()

        // Setup toolbar
        setupToolbar()

        // Listen for fragment result
        supportFragmentManager.setFragmentResultListener("approvalResult", this) { _, bundle ->
            val dataChanged = bundle.getBoolean("dataChanged")
            if (dataChanged) {
                fetchDataBasedOnUserType()
            }
        }

        viewModel.isLoading.observe(this, Observer { isLoading ->
            showLoading(isLoading)
        })

        viewModel.error.observe(this, Observer { error ->
            error?.let {
                Log.e(TAG, it)
                Toast.makeText(this, "Error: $it", Toast.LENGTH_SHORT).show()
            }
        })

        // Load initial data based on userType
        fetchDataBasedOnUserType()
        Log.d(TAG, "ApprovalActivity created and fetchItems called")
    }

    private fun setupTabs() {
        binding.tlCategory.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.let {
                    val selectedStatus = mapStatus(it.text.toString())
                    dataArrayList.clear() // Clear existing data
                    fetchDataBasedOnUserType(selectedStatus)
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun mapStatus(status: String): String {
        return when (status) {
            "Menunggu" -> "Pending"
            "Ditolak" -> "Rejected"
            "Disetujui" -> "Approved"
            else -> status // Return the original status if not mapped
        }
    }

    private fun setupRecyclerView() {
        dataArrayList = ArrayList()

        adapter = ApprovalAdapter(this, dataArrayList, viewModel) { dataModel ->
            val intent = Intent(this, ApprovalDetailActivity::class.java).apply {
                putExtra("ITEM_ID", dataModel.id)
                putExtra("USER_TYPE", userType)
            }
            startActivity(intent)
        }

        binding.rvData.adapter = adapter
        binding.rvData.layoutManager = LinearLayoutManager(this)

        // Observe data changes
        viewModel.items.observe(this, Observer { items ->
            if (items != null && items.isNotEmpty()) {
                dataArrayList.clear()
                dataArrayList.addAll(items)
                adapter.updateData(dataArrayList)
                binding.rvData.visibility = View.VISIBLE
                binding.tvEmpty.visibility = View.GONE
            } else {
                Log.d(TAG, "No items found")
                binding.rvData.visibility = View.GONE
                binding.tvEmpty.visibility = View.VISIBLE
            }
            applySearchFilter()
        })
    }

    private fun applySearchFilter() {
        adapter.filter.filter(currentSearchQuery)
    }

    // enable setupToolbar as actionbar
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = if (userType == "user") "Submission" else "Approval"
    }

    private fun fetchDataBasedOnUserType(selectedStatus: String = "Pending") {
        if (userType == "user") {
            val userId = firebaseAuth.currentUser?.uid
            userId?.let {
                viewModel.fetchItemsByUserAndStatus(it, selectedStatus)
            }
        } else {
            viewModel.fetchItemsByStatus(selectedStatus)
        }
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == DETAIL_REQUEST_CODE && resultCode == RESULT_OK) {
            fetchDataBasedOnUserType()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.onCleared() // Cancel ongoing operations
    }
}