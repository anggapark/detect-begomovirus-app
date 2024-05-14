package com.ipb.simpt.ui.add

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.ipb.simpt.R
import com.ipb.simpt.data.model.CategoryModel
import com.ipb.simpt.databinding.ActivityAddDataBinding
import com.ipb.simpt.utils.Extensions.toast
import com.ipb.simpt.utils.FileHelper

class AddDataActivity : AppCompatActivity() {

    // view binding
    private lateinit var binding: ActivityAddDataBinding

    // toolbar
    private lateinit var toolbar: Toolbar

    // firebase auth
    private lateinit var firebaseAuth: FirebaseAuth

    // arraylist to hold data categories
    private lateinit var categoryArrayList: ArrayList<CategoryModel>

    // uri of picked img
    private var imgUri: Uri? = null

    //TAG
    private val TAG = "DATA_ADD_TAG"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddDataBinding.inflate(layoutInflater)
        setContentView(binding.root)

        toolbar()
        enableEdgeToEdge()

        // init firebase auth
        firebaseAuth = FirebaseAuth.getInstance()

        // Load the categories
        loadDataCategories()

        // handle click
        setupAction()
    }

    private fun setupAction() {
        // show category pick dialog
        binding.tvCategory.setOnClickListener {
            categoryPickDialog()
        }

        // pick image intent
        binding.btnGallery.setOnClickListener {
            imagePickIntent()
        }

        // start uploading data
        binding.btnSubmit.setOnClickListener {
            addData()
        }
    }

    private var title = ""
    private var description = ""
    private var category = ""

    // TODO: EditText added is not a TextInputEditText. Please switch to using that class instead.
    private fun addData() {
        // validate data
        Log.d(TAG, "addData: validating data")

        //get data
        category = binding.tvCategory.text.toString().trim()
        title = binding.edAddName.text.toString().trim()
        description = binding.edAddDescription.text.toString().trim()

        // validate data
        if (category.isEmpty()) {
            toast("Pick Category")
        } else if (title.isEmpty()) {
            toast("Enter Name")
        } else if (description.isEmpty()) {
            toast("Enter Description")
        } else if (imgUri == null) {
            toast("Pick Image")
        } else {
            // data validated, begin upload
            uploadImgToStorage()
        }
    }

    private fun uploadImgToStorage() {
        // Upload image to firebase storage
        Log.d(TAG, "uploadImgToStorage: uploading to storage")

        showLoading(true)

        // timestamp
        val timestamp = System.currentTimeMillis()

        // path of img in firebase storage
        val filePathAndName = "Categories/$timestamp"

        // storage reference
        val storageReference = FirebaseStorage.getInstance().getReference(filePathAndName)
        storageReference.putFile(imgUri!!)
            .addOnSuccessListener { taskSnapshot ->
                Log.d(TAG, "uploadImgToStorage: Image uploaded now getting url")

                // Get url of uploaded image
                val uriTask: Task<Uri> = taskSnapshot.storage.downloadUrl
                while (!uriTask.isSuccessful);
                val uploadedImgUrl = "${uriTask.result}"

                uploadImgInfoToDb(uploadedImgUrl, timestamp)
            }
            .addOnFailureListener { e ->
                Log.d(TAG, "uploadImgToStorage: failed to upload due to ${e.message}")
                showLoading(false)
                toast("Failed to upload due to ${e.message}")
            }
    }

    private fun uploadImgInfoToDb(uploadedImgUrl: String, timestamp: Long) {
        //Upload image to firebase db
        Log.d(TAG, "uploadImgInfoToDb: uploading to db")

        // uid of current user
        val uid = firebaseAuth.uid

        // setup data to upload
        val hashMap: HashMap<String, Any> = HashMap()
        hashMap["uid"] = "$uid"
        hashMap["id"] = "$timestamp"
        hashMap["categoryId"] = selectedCategoryId
        hashMap["title"] = title
        hashMap["description"] = description
        hashMap["url"] = uploadedImgUrl
        hashMap["timestamp"] = timestamp

        // db reference DB > Categories > CategoryId > (Data Info)
        // TODO: Rework this reference later
        // TODO: After uploading, intent to Cek Pengajuan

        val db = FirebaseFirestore.getInstance()
        db.collection("Data")
            .document("$timestamp")
            .set(hashMap)
            .addOnSuccessListener {
                Log.d(TAG, "uploadImgInfoToDb: uploaded to db")
                showLoading(false)
                toast("Uploaded")
                imgUri = null
            }
            .addOnFailureListener { e ->
                Log.d(TAG, "uploadImgInfoToDb: failed to upload due to ${e.message}")
                showLoading(false)
                toast("Failed to upload due to ${e.message}")
            }
    }

    private fun loadDataCategories() {
        Log.d(TAG, "loadDataCategories: Loading Data")

        // init arraylist
        categoryArrayList = ArrayList()

        // db reference to load data
        val db = FirebaseFirestore.getInstance()
        db.collection("Categories")
            .get()
            .addOnSuccessListener { documents ->
                // clear list before adding data
                categoryArrayList.clear()
                for (document in documents) {
                    // get data as model
                    val model = document.toObject(CategoryModel::class.java)

                    // add to arraylist
                    categoryArrayList.add(model)
                    Log.d(TAG, "onDataChange: ${model.category}")
                }
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents: ", exception)
            }
    }

    private var selectedCategoryId = ""
    private var selectedCategoryTitle = ""
    private fun categoryPickDialog() {
        Log.d(TAG, "dataPickDialog: Showing data category pick dialog")

        // het string array of data from arraylist
        val dataArray = arrayOfNulls<String>(categoryArrayList.size)
        for (i in categoryArrayList.indices) {
            dataArray[i] = categoryArrayList[i].category
        }

        // alert dialog
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Pick Category")
            .setItems(dataArray) { dialog, which ->
                // handle item click
                // get clicked item
                selectedCategoryId = categoryArrayList[which].id
                selectedCategoryTitle = categoryArrayList[which].category

                // set category to text view
                binding.tvCategory.text = selectedCategoryTitle

                Log.d(TAG, "categoryPickDialog: Selected Category ID: ${selectedCategoryId}")
                Log.d(TAG, "categoryPickDialog: Selected Category Title: ${selectedCategoryTitle}")
            }
            .show()
    }

    private fun imagePickIntent() {
        Log.d(TAG, "imagePickIntent: starting image pick intent")

        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        imageActivityResultLauncher.launch(intent)
    }

    val imageActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
        ActivityResultCallback<ActivityResult> { result ->
            if (result.resultCode == RESULT_OK) {
                Log.d(TAG, "Image Picked")
                imgUri = result.data!!.data

                // Convert the image URI to a File object
                val imgFile = FileHelper.uriToFile(imgUri!!, this)

                // Reduce the size of the image file
                val reducedImgFile = FileHelper.reduceFileImage(imgFile)

                // Set the picked image to your ImageView
                binding.ivPreview.setImageURI(Uri.fromFile(reducedImgFile))
            } else {
                Log.d(TAG, "Image pick cancelled")
                Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show()
            }
        }
    )

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

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }
}