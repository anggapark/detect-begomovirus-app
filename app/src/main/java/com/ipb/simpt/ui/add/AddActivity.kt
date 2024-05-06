package com.ipb.simpt.ui.add

import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.firebase.auth.FirebaseAuth
import com.ipb.simpt.R
import com.ipb.simpt.data.model.CategoryModel
import com.ipb.simpt.databinding.ActivityAddBinding

class AddActivity : AppCompatActivity() {

    // view binding
    private lateinit var binding: ActivityAddBinding

    // toolbar
    private lateinit var toolbar: Toolbar

    // firebase auth
    private lateinit var firebaseAuth: FirebaseAuth

    // arraylist to hold data categories
    private lateinit var dataArrayList: ArrayList<CategoryModel>

    // uri of picked img
    private var imgUri: Uri? = null

    //TAG
    private val TAG = "DATA_ADD_TAG"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAddBinding.inflate(layoutInflater)
        setContentView(binding.root)

        toolbar()


    }


    private fun toolbar() {
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setTitle(R.string.home_add)
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