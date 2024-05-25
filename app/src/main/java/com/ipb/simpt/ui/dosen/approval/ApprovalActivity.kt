package com.ipb.simpt.ui.dosen.approval

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
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

    //TAG
    companion object {
        private const val TAG = "ApprovalActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityApprovalBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()

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
                    viewModel.fetchItemsByStatus(selectedStatus)
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun setupRecyclerView() {
        // init arraylist
        dataArrayList = ArrayList()

        adapter = ApprovalAdapter(this, dataArrayList, viewModel) { dataModel ->
            val intent = Intent(this, ApprovalDetailActivity::class.java).apply {
                putExtra("ITEM_ID", dataModel.id)
            }
            startActivity(intent)
        }

        binding.rvData.adapter = adapter
        binding.rvData.layoutManager = LinearLayoutManager(this)

        viewModel.items.observe(this, Observer { items ->
            if (items != null && items.isNotEmpty()) {
                adapter.updateData(ArrayList(items))
                Log.d(TAG, "RecyclerView updated with ${items.size} items")
            } else {
                Log.d(TAG, "No items found")
            }
        })

//        viewModel.items.observe(this, Observer { items ->
//            adapter.submitList(items)
//        })
    }


    // enable setupToolbar as actionbar
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Approval"
    }

    // TODO: Search yg lebih advanced. by komoditas, by penyakit, by deskripsi, by user

    private fun toggleLayout() {
        // Toggle between grid and list layout
        if (binding.rvData.layoutManager is LinearLayoutManager) {
            binding.rvData.layoutManager = GridLayoutManager(this, 2)
        } else {
            binding.rvData.layoutManager = LinearLayoutManager(this)
        }
        adapter.notifyItemRangeChanged(0, adapter.itemCount)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_approval, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressedDispatcher.onBackPressed()
                return true
            }

            R.id.action_filter -> {
                true
            }

            R.id.action_layout -> {
                toggleLayout()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }
}