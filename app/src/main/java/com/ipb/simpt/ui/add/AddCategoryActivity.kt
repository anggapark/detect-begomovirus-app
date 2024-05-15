package com.ipb.simpt.ui.add

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
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
    private lateinit var pathogen: String
    private lateinit var name: String

    //TAG
    private val TAG = "CATEGORY_ADD_TAG"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddCategoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()
        toolbar()

        // init firebase auth
        firebaseAuth = FirebaseAuth.getInstance()

        // handle click
        setupAction()
    }

    private fun setupAction() {
        // set up category selection dialog
        binding.tvCategory.setOnClickListener {
            showCategoryDialog()
        }

        binding.tvPathogen.setOnClickListener {
            showPathogenDialog()
        }

        binding.btnSubmit.setOnClickListener {
            addCategory()
        }
    }

    private fun showCategoryDialog() {
        // create and show a dialog for category selection
        // when a category is selected, update the TextViews based on the selected category
        // if the selected category is "Kategori Pathogen", show the prerequisite category and name TextViews
        // otherwise, hide them
        val categories = arrayOf("Komoditas", "Penyakit", "Gejala Penyakit", "Kategori Pathogen")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Choose a category")
        builder.setItems(categories) { dialog, which ->
            binding.tvCategory.text = categories[which]
            if (categories[which] == "Kategori Pathogen") {
                binding.tvPathogenTitle.visibility = View.VISIBLE
                binding.tvPathogen.visibility = View.VISIBLE
            } else {
                binding.tvPathogenTitle.visibility = View.GONE
                binding.tvPathogen.visibility = View.GONE
            }
        }
        builder.show()
    }

    private fun showPathogenDialog() {
        // create and show a dialog for pathogen selection
        val pathogens = arrayOf("Virus", "Bakteri", "Cendawan", "Nematoda", "Fitoplasma")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Choose a pathogen")
        builder.setItems(pathogens) { dialog, which ->
            binding.tvPathogen.text = pathogens[which]
        }
        builder.show()
    }

    private fun addCategory() {
        // validate data
        Log.d(TAG, "addCategory: validating data")

        // get data
        category = binding.tvCategory.text.toString().trim()
        pathogen = binding.tvPathogen.text.toString().trim()
        name = binding.edAddName.text.toString().trim()

        // validate data
        if (category.isEmpty()) {
            toast("Pick Category")
        } else if (category == "Kategori Pathogen" && pathogen.isEmpty()) {
            toast("Pick Pathogen")
        } else if (name.isEmpty()) {
            toast("Enter Name")
        } else {
            // data validated, begin upload
            uploadCategory()
        }
    }

    private fun uploadCategory() {
        Log.d(TAG, "uploadCategory: uploading category to db")

        // Show loading progress bar
        showLoading(true)

        // get timestamp
        val timestamp = System.currentTimeMillis()

        // setup data to add in firebase db
        val hashMap = HashMap<String, Any>()
        hashMap["id"] = "$timestamp"
        hashMap["name"] = name
        hashMap["timestamp"] = timestamp
        hashMap["uid"] = "${firebaseAuth.uid}"


        // TODO: After uploading, intent to Dosen Show
        val db = FirebaseFirestore.getInstance()
        if (category == "Kategori Pathogen") {
            db.collection("Categories")
                .document(category)
                .collection(pathogen)
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
            db.collection("Categories")
                .document(category)
                .collection("Items")
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

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }
}