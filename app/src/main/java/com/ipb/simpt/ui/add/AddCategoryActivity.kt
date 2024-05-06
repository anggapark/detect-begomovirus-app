package com.ipb.simpt.ui.add

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.ipb.simpt.R
import com.ipb.simpt.databinding.ActivityAddCategoryBinding
import com.ipb.simpt.utils.Extensions.toast

class AddCategoryActivity : AppCompatActivity() {

    // view binding
    private lateinit var binding: ActivityAddCategoryBinding

    // toolbar
    private lateinit var toolbar: Toolbar

    // firebase auth
    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var category: String
    private lateinit var cName: String
    private lateinit var categoryInputsArray: Array<EditText>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddCategoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()
        toolbar()

        // init firebase auth
        firebaseAuth = FirebaseAuth.getInstance()

        // input hint error for empty box
        categoryInputsArray = arrayOf(
            binding.etKategori,
            binding.etNama,
        )

        binding.submitButton.setOnClickListener {
            setupAction()
        }
    }

    // check if there's empty box
    private fun notEmpty(): Boolean = binding.etKategori.text.toString().trim().isNotEmpty() &&
            binding.etNama.text.toString().trim().isNotEmpty()


    private fun setupAction() {
        // get data
        category = binding.etKategori.text.toString().trim()
        cName = binding.etNama.text.toString().trim()


        // Show loading progress bar
        showLoading(true)

        if (notEmpty()) {
            // get timestamp
            val timestamp = System.currentTimeMillis()

            // setup data to add in firebase db
            val hashMap = HashMap<String, Any>()
            hashMap["id"] = "$timestamp"
            hashMap["category"] = category
            hashMap["cName"] = cName
            hashMap["timestamp"] = timestamp
            hashMap["uid"] = "${firebaseAuth.uid}"

            // TODO: After uploading, intent to Dosen Show
            // add to firestore db: Database Root > Categories > Category name > Data
            val db = FirebaseFirestore.getInstance()
            db.collection("Categories")
                .document("$timestamp")
                .set(hashMap)
                .addOnSuccessListener {
                    // added successfully
                    showLoading(false)
                    toast("Added successfully !")

                }
                .addOnFailureListener { e ->
                    // failed to add
                    showLoading(false)
                    toast("Failed saving user info due to ${e.message}")
                }
        } else {
            showLoading(false)
            categoryInputsArray.forEach { input ->
                if (input.text.toString().trim().isEmpty()) {
                    input.error = "${input.hint} is required"
                }
            }
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

    // TODO: Troublshoot progress bar  not on the middle of screen
    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }
}