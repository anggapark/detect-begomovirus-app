package com.ipb.simpt.ui.admin

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.ipb.simpt.R
import com.ipb.simpt.databinding.ActivityAdminHomeBinding
import com.ipb.simpt.databinding.ActivityApprovalBinding
import com.ipb.simpt.model.DataModel
import com.ipb.simpt.model.UserModel
import com.ipb.simpt.ui.adapter.AdminAdapter
import com.ipb.simpt.ui.adapter.ApprovalAdapter
import com.ipb.simpt.ui.auth.splash.WelcomeActivity
import com.ipb.simpt.ui.dosen.approval.ApprovalActivity
import com.ipb.simpt.ui.dosen.approval.ApprovalDetailActivity
import com.ipb.simpt.ui.dosen.approval.ApprovalViewModel
import com.ipb.simpt.ui.dosen.home.DosenHomeActivity
import java.util.Locale

class AdminHomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminHomeBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var userArrayList: ArrayList<UserModel>
    private lateinit var viewModel: AdminViewModel
    private lateinit var adapter: AdminAdapter
    private var backPressedTime: Long = 0

    private var currentSearchQuery: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        viewModel = ViewModelProvider(this).get(AdminViewModel::class.java)

        checkUser()
        setupToolbar()
        setupTabs()
        setupRecyclerView()

        // load dinitial data
        viewModel.fetchUserByType(getSelectedUserType())
    }

    private fun checkUser() {
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser == null) {
            // Not signed in, launch the Welcome activity
            startActivity(Intent(this, WelcomeActivity::class.java))
            finish()
        } else {
            // Logged in, check user type
            val db = FirebaseFirestore.getInstance()
            db.collection("Users")
                .document(firebaseUser.uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val userType = document.getString("userType")
                        if (userType != "admin") {
                            // Not a regular user, launch the Welcome activity
                            startActivity(Intent(this, WelcomeActivity::class.java))
                            finish()
                        }
                    } else {
                        Log.d(TAG, "No such document")
                    }
                }
                .addOnFailureListener { exception ->
                    Log.d(TAG, "get failed with ", exception)
                }
        }
    }

    private fun setupTabs() {
        binding.tlCategory.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.let {
                    val selectedUserType = it.text.toString().lowercase(Locale.getDefault())
                    userArrayList.clear()
                    viewModel.fetchUserByType(selectedUserType)
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun getSelectedUserType(): String {
        val selectedTab = binding.tlCategory.getTabAt(binding.tlCategory.selectedTabPosition)
        return selectedTab?.text.toString().lowercase(Locale.getDefault())
    }

    private fun setupRecyclerView() {
        userArrayList = ArrayList()

        adapter = AdminAdapter(this, userArrayList) { userModel ->
            val intent = Intent(this, AdminUserDetailActivity::class.java).apply {
                putExtra("USER_ID", userModel.uid)
            }
            startActivity(intent)
        }

        binding.rvData.adapter = adapter
        binding.rvData.layoutManager = LinearLayoutManager(this)

        // Observe data changes
        viewModel.items.observe(this, Observer { items ->
            if (items != null && items.isNotEmpty()) {
                userArrayList.clear()
                userArrayList.addAll(items)
                adapter.updateData(userArrayList)
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

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "Admin"
    }

    // TODO: Menu Icon -> Refresh, Log Out
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_admin, menu)
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
            R.id.action_refresh -> {
                viewModel.fetchUserByType(getSelectedUserType())
                true
            }
            R.id.action_logout -> {
                signOut()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun signOut() {
        firebaseAuth.signOut()
        startActivity(Intent(this, WelcomeActivity::class.java))
        Toast.makeText(this, "Signed Out", Toast.LENGTH_SHORT).show()
        this.finish()
    }

    override fun onBackPressed() {
        if (backPressedTime + 2000 > System.currentTimeMillis()) {
            super.onBackPressed()
            return
        } else {
            Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT).show()
        }

        backPressedTime = System.currentTimeMillis()
    }
}