//package com.ipb.simpt.ui.dosen.library
//
//import android.os.Bundle
//import android.text.Editable
//import android.text.TextWatcher
//import android.util.Log
//import androidx.activity.enableEdgeToEdge
//import androidx.appcompat.app.AppCompatActivity
//import com.google.firebase.firestore.FirebaseFirestore
//import com.ipb.simpt.data.model.DataModel
//import com.ipb.simpt.databinding.ActivityDosenDataListBinding
//import com.ipb.simpt.ui.adapter.DataAdminAdapter
//
//class DosenDataListActivity : AppCompatActivity() {
//
//    private companion object {
//        const val TAG = "DATA_LIST_ADMIN"
//    }
//
//    // category id, title
//    private lateinit var categoryId: String
//    private lateinit var category: String
//    private lateinit var title: String
//    private lateinit var binding: ActivityDosenDataListBinding
//
//    // arraylist to hold data
//    private lateinit var dataArrayList: ArrayList<DataModel>
//
//    // adapter
//    private lateinit var dataAdminAdapter: DataAdminAdapter
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        binding = ActivityDosenDataListBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//        enableEdgeToEdge()
//
//        //get from intent, that we pass from adapter
//        val intent = intent
//        categoryId = intent.getStringExtra("categoryId")!!
//        category = intent.getStringExtra("category")!!
//
//        // load data
//        loadDataList()
//
//        // search
//        binding.etSearch.addTextChangedListener(object : TextWatcher {
//            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
//
//            }
//
//            override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
//                // filter data
//                try {
//                    dataAdminAdapter.filter.filter(s)
//                } catch (e: Exception) {
//                    Log.d(TAG, "onTextChanged: ${e.message}")
//                }
//            }
//
//            override fun afterTextChanged(p0: Editable?) {
//
//            }
//
//        })
//    }
//
//    private fun loadDataList() {
//        // init arraylist
//        dataArrayList = ArrayList()
//
//        val db = FirebaseFirestore.getInstance()
//        db.collection("Data")
//            .whereEqualTo("categoryId", categoryId)
//            .get()
//            .addOnSuccessListener { documents ->
//                // clear list before start adding data in it
//                dataArrayList.clear()
//                for (document in documents) {
//                    // get data
//                    val model = document.toObject(DataModel::class.java)
//                    // add to list
//                    dataArrayList.add(model)
//                    Log.d(TAG, "onDataChange: ${model.categoryId}")
//                }
//                // setup adapter
//                dataAdminAdapter = DataAdminAdapter(this@DosenDataListActivity, dataArrayList)
//                binding.rvData.adapter = dataAdminAdapter
//            }
//            .addOnFailureListener { exception ->
//                Log.w(TAG, "Error getting documents: ", exception)
//            }
//    }
//
//}