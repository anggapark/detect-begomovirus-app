package com.ipb.simpt.ui.add

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
import com.ipb.simpt.utils.CategoryHandler
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

    // CategoryHandler
    private val categoryHandler = CategoryHandler(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddDataBinding.inflate(layoutInflater)
        setContentView(binding.root)

        toolbar()
        enableEdgeToEdge()

        // init firebase auth
        firebaseAuth = FirebaseAuth.getInstance()

        // handle click
        setupAction()
    }

    private var selectedKomoditasId: String? = null
    private var selectedPenyakitId: String? = null
    private var selectedPathogenId: String? = null
    private var selectedGejalaId: String? = null

    private fun setupAction() {
        // show komoditas category pick dialog
        binding.tvKomoditas.setOnClickListener {
            categoryHandler.showKomoditasDialog { selectedId, selectedKomoditas ->
                selectedKomoditasId = selectedId
                binding.tvKomoditas.text = selectedKomoditas
            }
        }

        // show penyakit category pick dialog
        binding.tvPenyakit.setOnClickListener {
            categoryHandler.showPenyakitDialog { selectedId, selectedPenyakit ->
                selectedPenyakitId = selectedId
                binding.tvPenyakit.text = selectedPenyakit
            }
        }

        // show kategori pathogen category pick dialog
        binding.tvKategoriPathogen.setOnClickListener {
            categoryHandler.showKategoriPathogenDialog { selectedKategoriPathogen ->
                binding.tvKategoriPathogen.text = selectedKategoriPathogen
                binding.tvPathogen.text = "" // Reset the Pathogen field
                selectedPathogenId = null // Clear the selected Pathogen ID
            }
        }

        // show pathogen category pick dialog
        binding.tvPathogen.setOnClickListener {
            val selectedKategoriPathogen = binding.tvKategoriPathogen.text.toString()
            if (selectedKategoriPathogen.isNotEmpty()) {
                categoryHandler.showPathogenDialog(selectedKategoriPathogen) { selectedId, selectedPathogen ->
                    selectedPathogenId = selectedId
                    binding.tvPathogen.text = selectedPathogen
                }
            } else {
                toast("Please select a Kategori Pathogen first")
            }
        }

        // show gejala category pick dialog
        binding.tvGejala.setOnClickListener {
            categoryHandler.showGejalaDialog { selectedId, selectedGejala ->
                selectedGejalaId = selectedId
                binding.tvGejala.text = selectedGejala
            }
        }

        // pick image intent
        binding.btnGallery.setOnClickListener {
            imagePickIntent()
        }

        binding.btnSubmit.setOnClickListener {
            binding.btnSubmit.isEnabled = false // disable the button when clicked
            if (selectedKomoditasId == null || selectedPenyakitId == null || selectedPathogenId == null || selectedGejalaId == null) {
                toast("Please select all categories")
                binding.btnSubmit.isEnabled = true // enable the button if data is not valid
            } else {
                addData(
                    selectedKomoditasId!!,
                    selectedPenyakitId!!,
                    selectedPathogenId!!,
                    selectedGejalaId!!
                )
            }
        }
    }

    // TODO: EditText added is not a TextInputEditText. Please switch to using that class instead.
    private fun addData(
        komoditasId: String,
        penyakitId: String,
        pathogenId: String,
        gejalaId: String
    ) {
        Log.d(TAG, "addData: validating data")

        val kategoriPathogen = binding.tvKategoriPathogen.text.toString().trim()
        val dataset = binding.edAddDataset.text.toString().trim()
        val deskripsi = binding.edAddDescription.text.toString().trim()

        // validate data
        if (komoditasId.isEmpty()) {
            toast("Pick Komoditas")
            binding.btnSubmit.isEnabled = true
        } else if (penyakitId.isEmpty()) {
            toast("Pick Penyakit")
            binding.btnSubmit.isEnabled = true
        } else if (kategoriPathogen.isEmpty()) {
            toast("Pick Kategori Pathogen")
            binding.btnSubmit.isEnabled = true
        } else if (pathogenId.isEmpty()) {
            toast("Pick Pathogen")
            binding.btnSubmit.isEnabled = true
        } else if (gejalaId.isEmpty()) {
            toast("Pick Gejala Penyakit")
            binding.btnSubmit.isEnabled = true
        } else if (dataset.isEmpty()) {
            toast("Enter Dataset")
            binding.btnSubmit.isEnabled = true
        } else if (deskripsi.isEmpty()) {
            toast("Enter Description")
            binding.btnSubmit.isEnabled = true
        } else if (imgUri == null) {
            toast("Upload Image")
            binding.btnSubmit.isEnabled = true
        } else {
            // data validated, begin upload
            uploadImgToStorage(
                komoditasId,
                penyakitId,
                kategoriPathogen,
                pathogenId,
                gejalaId,
                dataset,
                deskripsi
            )
        }
    }

    private fun uploadImgToStorage(
        komoditasId: String,
        penyakitId: String,
        kategoriPathogen: String,
        pathogenId: String,
        gejalaId: String,
        dataset: String,
        deskripsi: String
    ) {
        // Upload image to firebase storage
        Log.d(TAG, "uploadImgToStorage: uploading to storage")

        showLoading(true)

        // timestamp
        val timestamp = System.currentTimeMillis()

        // path of img in firebase storage
        val filePathAndName = "Images/$timestamp"

        // storage reference
        val storageReference = FirebaseStorage.getInstance().getReference(filePathAndName)
        storageReference.putFile(imgUri!!)
            .addOnSuccessListener { taskSnapshot ->
                Log.d(TAG, "uploadImgToStorage: Image uploaded now getting url")

                // Get url of uploaded image
                val uriTask: Task<Uri> = taskSnapshot.storage.downloadUrl
                while (!uriTask.isSuccessful);
                val uploadedImgUrl = "${uriTask.result}"

                uploadDataInfoToDb(
                    uploadedImgUrl,
                    timestamp,
                    komoditasId,
                    penyakitId,
                    kategoriPathogen,
                    pathogenId,
                    gejalaId,
                    dataset,
                    deskripsi
                )
            }
            .addOnFailureListener { e ->
                Log.d(TAG, "uploadImgToStorage: failed to upload due to ${e.message}")
                showLoading(false)
                toast("Failed to upload due to ${e.message}")
                binding.btnSubmit.isEnabled = true // enable the button if upload fails
            }
    }

    private fun uploadDataInfoToDb(
        uploadedImgUrl: String,
        timestamp: Long,
        komoditasId: String,
        penyakitId: String,
        kategoriPathogen: String,
        pathogenId: String,
        gejalaId: String,
        dataset: String,
        deskripsi: String
    ) {
        //Upload data ingfo to firestore
        Log.d(TAG, "uploadDataInfoToDb: uploading to db")

        // uid of current user
        val uid = firebaseAuth.uid

        // status of approval
        val status = "Pending"

        // setup data to upload
        val hashMap: HashMap<String, Any> = HashMap()
        hashMap["uid"] = "$uid"
        hashMap["id"] = "$timestamp"
        hashMap["komoditasId"] = komoditasId
        hashMap["penyakitId"] = penyakitId
        hashMap["kategoriPathogen"] = kategoriPathogen
        hashMap["pathogenId"] = pathogenId
        hashMap["gejalaId"] = gejalaId
        hashMap["dataset"] = dataset
        hashMap["deskripsi"] = deskripsi
        hashMap["url"] = uploadedImgUrl
        hashMap["timestamp"] = timestamp
        hashMap["status"] = status


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
                binding.ivPreview.setImageURI(null) // reset the preview image
                binding.ivDefault.visibility = View.VISIBLE // show the default image
                binding.btnSubmit.isEnabled = true // enable the button after data is uploaded
            }
            .addOnFailureListener { e ->
                Log.d(TAG, "uploadImgInfoToDb: failed to upload due to ${e.message}")
                showLoading(false)
                toast("Failed to upload due to ${e.message}")
                binding.btnSubmit.isEnabled = true // enable the button if upload fails
            }
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