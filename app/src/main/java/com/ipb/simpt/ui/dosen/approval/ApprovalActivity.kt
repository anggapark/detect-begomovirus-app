package com.ipb.simpt.ui.dosen.approval

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.ipb.simpt.R
import com.ipb.simpt.databinding.ActivityApprovalBinding
import com.ipb.simpt.model.DataModel
import com.ipb.simpt.ui.adapter.FilterDialogFragment
import com.ipb.simpt.ui.adapter.SubmissionAdapter
import com.ipb.simpt.utils.CategoryHandler
import com.ipb.simpt.utils.SearchHandler

class ApprovalActivity : AppCompatActivity() {

    // view binding
    private lateinit var binding: ActivityApprovalBinding

    // Declare the SearchHandler
    private lateinit var searchHandler: SearchHandler

    // firebase auth
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    // arraylist to hold data model
    private lateinit var adapter: SubmissionAdapter
    private lateinit var submissions: MutableList<DataModel>
    private lateinit var filteredSubmissions: MutableList<DataModel>

    // tab and category
    private lateinit var categoryHandler: CategoryHandler
    private var currentCategory: String? = null
    private var currentTab: String = "Komoditas"


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
        firestore = FirebaseFirestore.getInstance()

        // Setup Tabs
        setupTabs()

        // Setup Recycler View
        setupRecyclerView()

        // Setup toolbar
        setupToolbar()
        binding.tvCategory.text = getString(R.string.all_categories)
        currentCategory = null

        // load submission
        loadSubmissions()

    }

    private fun setupTabs() {
        binding.tlCategory.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                currentTab = when (tab?.position) {
                    0 -> "Komoditas"
                    1 -> "Penyakit"
                    else -> "Komoditas"
                }
                loadSubmissions()
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun setupRecyclerView() {
        categoryHandler = CategoryHandler(this)
        submissions = mutableListOf()
        filteredSubmissions = mutableListOf()
        adapter = SubmissionAdapter(filteredSubmissions) { submission ->
            openDetailActivity(submission)
        }

        binding.rvData.layoutManager = LinearLayoutManager(this)
        binding.rvData.adapter = adapter
    }

    private fun loadSubmissions() {
        val collectionRef = firestore.collection("Data")
        val query = collectionRef.whereEqualTo("type", currentTab)

        query.get()
            .addOnSuccessListener { querySnapshot ->
                submissions.clear()
                val tasks = mutableListOf<Task<DataModel>>()

                for (document in querySnapshot) {
                    val submission = document.toObject(DataModel::class.java)
                    if (currentCategory == null || submission.komoditasId == currentCategory || submission.penyakitId == currentCategory) {
                        tasks.add(completeSubmissionData(submission))
                    }
                }

                Tasks.whenAllSuccess<DataModel>(tasks)
                    .addOnSuccessListener { completedSubmissions ->
                        submissions.addAll(completedSubmissions)
                        filteredSubmissions.clear()
                        filteredSubmissions.addAll(submissions)
                        adapter.notifyDataSetChanged()
                    }
            }
            .addOnFailureListener { e ->
                Log.d(TAG, "Error fetching submissions", e)
            }
    }

    private fun completeSubmissionData(submission: DataModel): Task<DataModel> {
        val komoditasTask = firestore.collection("Categories").document("Komoditas")
            .collection("Items").document(submission.komoditasId).get()

        val penyakitTask = firestore.collection("Categories").document("Penyakit")
            .collection("Items").document(submission.penyakitId).get()

        val pathogenTask = firestore.collection("Categories").document("Kategori Pathogen")
            .collection(submission.kategoriPathogen).document(submission.pathogenId).get()

        val gejalaTask = firestore.collection("Categories").document("Gejala Penyakit")
            .collection("Items").document(submission.gejalaId).get()

        val uploaderTask = firestore.collection("Users").document(submission.uid).get()

        return Tasks.whenAllSuccess<DocumentSnapshot>(
            komoditasTask,
            penyakitTask,
            pathogenTask,
            gejalaTask,
            uploaderTask
        )
            .continueWith { task ->
                val results = task.result
                submission.komoditasId = results[0]?.getString("name") ?: submission.komoditasId
                submission.penyakitId = results[1]?.getString("name") ?: submission.penyakitId
                submission.pathogenId = results[2]?.getString("name") ?: submission.pathogenId
                submission.gejalaId = results[3]?.getString("name") ?: submission.gejalaId
                submission.uid = results[4]?.getString("userName") ?: submission.uid
                submission
            }
    }

    // enable setupToolbar as actionbar
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Approval"

        binding.tvCategory.setOnClickListener {
            showCategoryPicker()
        }
        // TODO: bikin search di toolbar
        // TODO: filter sort by pending; approved; rejected
        // TODO: change layout grid <=> list
    }

    private fun showCategoryPicker() {
        when (currentTab) {
            "Komoditas" -> categoryHandler.showKomoditasDialog { id, name ->
                currentCategory = id
                binding.tvCategory.text = name
                loadSubmissions()
            }
            "Penyakit" -> categoryHandler.showPenyakitDialog { id, name ->
                currentCategory = id
                binding.tvCategory.text = name
                loadSubmissions()
            }
            else -> {
                currentCategory = null
                binding.tvCategory.text = getString(R.string.all_categories)
                loadSubmissions()
            }
        }
    }

    private fun showFilterDialog() {
        // Show dialog to filter data by status (Pending, Approved, Rejected)
        // Implement the filter logic based on status
        val dialog = FilterDialogFragment()
        dialog.show(supportFragmentManager, "FilterDialogFragment")
    }

    // TODO: Search yg lebih advanced. by komoditas, by penyakit, by deskripsi, by user
    private fun searchSubmissions(query: String) {
        searchHandler.search(query)
    }

    private fun toggleLayout() {
        // Toggle between grid and list layout
        if (binding.rvData.layoutManager is LinearLayoutManager) {
            binding.rvData.layoutManager = GridLayoutManager(this, 2)
        } else {
            binding.rvData.layoutManager = LinearLayoutManager(this)
        }
        adapter.notifyItemRangeChanged(0, adapter.itemCount)
    }

    private fun openDetailActivity(submission: DataModel) {
        val intent = Intent(this, ApprovalDetailActivity::class.java)
        intent.putExtra("submissionId", submission.id)
        startActivity(intent)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_approval, menu)

        // Setup search
        val searchItem = menu?.findItem(R.id.action_search)
        val searchView = searchItem?.actionView as? SearchView

        searchView?.queryHint = "Search..."
        searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { searchSubmissions(it) }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let { searchSubmissions(it) }
                return true
            }
        })

        return true
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_filter -> {
                showFilterDialog()
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