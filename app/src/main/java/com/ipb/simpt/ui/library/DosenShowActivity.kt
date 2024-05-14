package com.ipb.simpt.ui.library

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.ipb.simpt.R
import com.ipb.simpt.data.model.CategoryModel
import com.ipb.simpt.databinding.ActivityDosenShowBinding
import com.ipb.simpt.ui.adapter.CategoryAdapter
import com.ipb.simpt.ui.splash.WelcomeActivity

class DosenShowActivity : AppCompatActivity() {

    // view binding
    private lateinit var binding: ActivityDosenShowBinding

    // toolbar
    private lateinit var toolbar: Toolbar

    // firebase auth
    private lateinit var firebaseAuth: FirebaseAuth

    // arraylist to hold categories
    private lateinit var categoryArrayList: ArrayList<CategoryModel>

    // adapter
    private lateinit var adapter: CategoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDosenShowBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        toolbar()

        // init firebase auth
        firebaseAuth = FirebaseAuth.getInstance()


        // Load the categories
        loadCategories()

        // search
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

    private fun loadCategories() {
        // init arraylist
        categoryArrayList = ArrayList()

        // Get all categories from Firestore
        val db = FirebaseFirestore.getInstance()
        db.collection("Categories")
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val model = document.toObject(CategoryModel::class.java)
                    categoryArrayList.add(model)
                }
                // setup adapter
                adapter = CategoryAdapter(this, categoryArrayList)
                // set adapter to recyclerview
                binding.rvCategories.adapter = adapter
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents: ", exception)
            }
    }


    // enable toolbar as actionbar
    private fun toolbar() {
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
}