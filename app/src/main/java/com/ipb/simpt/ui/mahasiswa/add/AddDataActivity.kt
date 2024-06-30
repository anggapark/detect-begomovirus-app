package com.ipb.simpt.ui.mahasiswa.add

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.ipb.simpt.R
import com.ipb.simpt.databinding.ActivityAddDataBinding
import com.ipb.simpt.repository.DataRepository
import com.ipb.simpt.ui.dosen.approval.ApprovalActivity
import com.ipb.simpt.ui.dosen.approval.ApprovalDetailActivity
import com.ipb.simpt.utils.CategoryHandler
import com.ipb.simpt.utils.Extensions.toast
import com.ipb.simpt.utils.FileHelper

class AddDataActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddDataBinding
    private lateinit var categoryHandler: CategoryHandler
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var toolbar: Toolbar
    private val viewModel: AddDataViewModel by viewModels()

    private var itemId: String? = null
    private var userType: String = "user"
    private var selectedKomoditasId: String? = null
    private var selectedPenyakitId: String? = null
    private var selectedPathogenId: String? = null
    private var selectedGejalaId: String? = null

    companion object {
        private const val TAG = "AddDataActivity"
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddDataBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupToolbar()

        firebaseAuth = FirebaseAuth.getInstance()
        categoryHandler = CategoryHandler(this)
        itemId = intent.getStringExtra("ITEM_ID")
        userType = intent.getStringExtra("USER_TYPE") ?: userType
        setupUI()

        if (itemId != null) {
            setupEdit(itemId!!)
        }

        setupViewModel()
        setupAction()
    }

    private fun setupUI() {
        if (itemId != null) {
            // Edit mode
            supportActionBar?.setTitle(R.string.title_edit_data)
            binding.btnAddImage.text = getString(R.string.hint_ganti_gambar)
            binding.btnAddDatasetImage.text = getString(R.string.hint_ganti_gambar_dataset)
            binding.btnSave.visibility = View.VISIBLE
            binding.btnSubmit.visibility = View.GONE
        } else {
            // Add mode
            supportActionBar?.setTitle(R.string.home_add)
            binding.btnAddImage.text = getString(R.string.hint_gambar)
            binding.btnAddDatasetImage.text = getString(R.string.hint_gambar_dataset)
            binding.btnSave.visibility = View.GONE
            binding.btnSubmit.visibility = View.VISIBLE
        }
    }

    private fun setupEdit(itemId: String) {
        showLoading(true)

        val repository = DataRepository()
        repository.fetchItemDetail(itemId) { dataModel ->
            runOnUiThread {
                showLoading(false)
                binding.tvKomoditas.text = dataModel.komoditasName
                binding.tvPenyakit.text = dataModel.penyakitName
                binding.tvKategoriPathogen.text = dataModel.kategoriPathogen
                binding.tvPathogen.text = dataModel.pathogenName
                binding.tvGejala.text = dataModel.gejalaName
                binding.edAddDataset.setText(dataModel.dataset)
                binding.edAddDescription.setText(dataModel.deskripsi)
                val imageUrl = dataModel.url
                Glide.with(this).load(imageUrl).into(binding.ivPreview)

                // Set the selected IDs
                selectedKomoditasId = dataModel.komoditasId
                selectedPenyakitId = dataModel.penyakitId
                selectedPathogenId = dataModel.pathogenId
                selectedGejalaId = dataModel.gejalaId
            }
        }
    }


    private fun setupAction() {
        binding.apply {
            tvKomoditas.setOnClickListener {
                categoryHandler.showKomoditasDialog { selectedId, selectedKomoditas ->
                    selectedKomoditasId = selectedId
                    tvKomoditas.text = selectedKomoditas
                }
            }

            tvPenyakit.setOnClickListener {
                categoryHandler.showPenyakitDialog { selectedId, selectedPenyakit ->
                    selectedPenyakitId = selectedId
                    tvPenyakit.text = selectedPenyakit
                }
            }

            tvKategoriPathogen.setOnClickListener {
                categoryHandler.showKategoriPathogenDialog { selectedKategoriPathogen ->
                    tvKategoriPathogen.text = selectedKategoriPathogen
                    tvPathogen.text = "" // Reset the Pathogen field
                    selectedPathogenId = null // Clear the selected Pathogen ID
                }
            }

            tvPathogen.setOnClickListener {
                val selectedKategoriPathogen = tvKategoriPathogen.text.toString()
                if (selectedKategoriPathogen.isNotEmpty()) {
                    categoryHandler.showPathogenDialog(selectedKategoriPathogen) { selectedId, selectedPathogen ->
                        selectedPathogenId = selectedId
                        tvPathogen.text = selectedPathogen
                    }
                } else {
                    toast("Please select a Kategori Pathogen first")
                }
            }

            tvGejala.setOnClickListener {
                categoryHandler.showGejalaDialog { selectedId, selectedGejala ->
                    selectedGejalaId = selectedId
                    tvGejala.text = selectedGejala
                }
            }

            btnAddImage.setOnClickListener {
                imagePickIntent()
            }

            btnAddDatasetImage.setOnClickListener {
                val nonNullItemId = itemId ?: ""
                val bottomSheetFragment = AddDataBottomSheetFragment.newInstance(nonNullItemId)
                bottomSheetFragment.show(supportFragmentManager, bottomSheetFragment.tag)
            }


            btnSubmit.setOnClickListener {
                btnSubmit.isEnabled = false // Disable the button when clicked
                addOrUpdateData(
                    selectedKomoditasId ?: "",
                    selectedPenyakitId ?: "",
                    selectedPathogenId ?: "",
                    selectedGejalaId ?: ""
                )
            }

            btnSave.setOnClickListener {
                btnSave.isEnabled = false // Disable the button when clicked
                addOrUpdateData(
                    selectedKomoditasId ?: "",
                    selectedPenyakitId ?: "",
                    selectedPathogenId ?: "",
                    selectedGejalaId ?: "",
                    true
                )
            }
        }
    }

    private fun addOrUpdateData(
        komoditasId: String,
        penyakitId: String,
        pathogenId: String,
        gejalaId: String,
        isUpdate: Boolean = false
    ) {
        Log.d(TAG, if (isUpdate) "updateData" else "addData: validating data")

        val kategoriPathogen = binding.tvKategoriPathogen.text.toString().trim()
        val dataset = binding.edAddDataset.text.toString().trim()
        val deskripsi = binding.edAddDescription.text.toString().trim()
        val activityImgUri = viewModel.activityImgUri.value
        val bottomSheetImgUri = viewModel.bottomSheetImgUri.value
        when {
            komoditasId.isEmpty() -> toast("Pick Komoditas")
            penyakitId.isEmpty() -> toast("Pick Penyakit")
            kategoriPathogen.isEmpty() -> toast("Pick Kategori Pathogen")
            pathogenId.isEmpty() -> toast("Pick Pathogen")
            gejalaId.isEmpty() -> toast("Pick Gejala Penyakit")
            dataset.isEmpty() -> toast("Enter Dataset")
            deskripsi.isEmpty() -> toast("Enter Description")
            activityImgUri == null && !isUpdate -> toast("Upload Data Image")
            bottomSheetImgUri == null && !isUpdate -> toast("Upload Dataset Acuan Image")
            else -> {
                // Data validated, begin upload or update
                uploadImgToStorage(
                    komoditasId,
                    penyakitId,
                    kategoriPathogen,
                    pathogenId,
                    gejalaId,
                    dataset,
                    deskripsi,
                    isUpdate
                )
            }
        }

        binding.btnSave.isEnabled = true
        binding.btnSubmit.isEnabled = true

    }

    private fun uploadImgToStorage(
        komoditasId: String,
        penyakitId: String,
        kategoriPathogen: String,
        pathogenId: String,
        gejalaId: String,
        dataset: String,
        deskripsi: String,
        isUpdate: Boolean = false
    ) {
        showLoading(true)
        val timestamp = System.currentTimeMillis()
        val imagePathAndName = "Images/$timestamp"
        val datasetPathAndName = "Dataset/$timestamp"
        val storageReference = FirebaseStorage.getInstance().getReference(imagePathAndName)
        val datasetStorageReference = FirebaseStorage.getInstance().getReference(datasetPathAndName)

        val activityImgUri = viewModel.activityImgUri.value
        val bottomSheetImgUri = viewModel.bottomSheetImgUri.value

        val uploadTasks = mutableListOf<Task<Uri>>()

        activityImgUri?.let { uri ->
            val uploadTask = storageReference.putFile(uri)
                .continueWithTask { task ->
                    if (!task.isSuccessful) {
                        task.exception?.let { throw it }
                    }
                    storageReference.downloadUrl
                }
            uploadTasks.add(uploadTask)
        }

        bottomSheetImgUri?.let { uri ->
            val uploadTask = datasetStorageReference.putFile(uri)
                .continueWithTask { task ->
                    if (!task.isSuccessful) {
                        task.exception?.let { throw it }
                    }
                    datasetStorageReference.downloadUrl
                }
            uploadTasks.add(uploadTask)
        }

        Tasks.whenAllComplete(uploadTasks)
            .addOnSuccessListener { tasks ->
                val uploadedImgUrls = tasks.filter { it.isSuccessful }
                    .mapNotNull { (it.result as? Uri)?.toString() }

                val uploadedActivityImgUrl = uploadedImgUrls.getOrNull(0) ?: ""
                val uploadedBottomSheetImgUrl = uploadedImgUrls.getOrNull(1) ?: ""

                uploadDataInfoToDb(
                    uploadedActivityImgUrl,
                    uploadedBottomSheetImgUrl,
                    timestamp,
                    komoditasId,
                    penyakitId,
                    kategoriPathogen,
                    pathogenId,
                    gejalaId,
                    dataset,
                    deskripsi,
                    isUpdate
                )
            }
            .addOnFailureListener { e ->
                showLoading(false)
                toast("Failed to upload due to ${e.message}")
                binding.btnSubmit.isEnabled = true
                binding.btnSave.isEnabled = true
            }
    }

    private fun uploadDataInfoToDb(
        uploadedImgUrl: String,
        uploadedDatasetImgUrl: String,
        timestamp: Long,
        komoditasId: String,
        penyakitId: String,
        kategoriPathogen: String,
        pathogenId: String,
        gejalaId: String,
        dataset: String,
        deskripsi: String,
        isUpdate: Boolean = false
    ) {
        val db = FirebaseFirestore.getInstance()
        val uid = firebaseAuth.uid
        val status = "Pending"

        // setup data to upload
        val hashMap: HashMap<String, Any> = HashMap()
        hashMap["uid"] = "$uid"
        hashMap["komoditasId"] = komoditasId
        hashMap["penyakitId"] = penyakitId
        hashMap["kategoriPathogen"] = kategoriPathogen
        hashMap["pathogenId"] = pathogenId
        hashMap["gejalaId"] = gejalaId
        hashMap["dataset"] = dataset
        hashMap["deskripsi"] = deskripsi
        hashMap["status"] = status

        if (uploadedImgUrl.isNotEmpty()) {
            hashMap["url"] = uploadedImgUrl
        }

        if (uploadedDatasetImgUrl.isNotEmpty()) {
            hashMap["datasetUrl"] = uploadedDatasetImgUrl
        }

        if (isUpdate && itemId != null) {
            db.collection("Data")
                .document(itemId!!)
                .update(hashMap as Map<String, Any>)
                .addOnSuccessListener {
                    showLoading(false)
                    toast("Data updated")

                    // Navigate back to DetailActivity after saving
                    val intent = Intent(this, ApprovalDetailActivity::class.java).apply {
                        putExtra("ITEM_ID", itemId)
                        putExtra("USER_TYPE", userType)
                    }
                    startActivity(intent)
                    finish()
                }
                .addOnFailureListener { e ->
                    showLoading(false)
                    toast("Failed to update due to ${e.message}")
                    binding.btnSave.isEnabled = true // enable the button if update fails
                }
        } else {
            hashMap["id"] = "$timestamp"
            hashMap["timestamp"] = timestamp
            db.collection("Data")
                .document("$timestamp")
                .set(hashMap)
                .addOnSuccessListener {
                    showLoading(false)
                    toast("Uploaded")
                    resetForm()
                }
                .addOnFailureListener { e ->
                    showLoading(false)
                    toast("Failed to upload due to ${e.message}")
                    binding.btnSubmit.isEnabled = true // enable the button if upload fails
                }
        }
    }

    private fun resetForm() {
        viewModel.resetImgUris()
        binding.ivPreview.setImageURI(null) // reset the preview image
        binding.ivDefault.visibility = View.VISIBLE // show the default image
        binding.btnSubmit.isEnabled = true // enable the button after data is uploaded
        binding.btnSave.isEnabled = true

        binding.tvKomoditas.text = ""
        binding.tvPenyakit.text = ""
        binding.tvKategoriPathogen.text = ""
        binding.tvPathogen.text = ""
        binding.tvGejala.text = ""
        binding.edAddDataset.setText("")
        binding.edAddDescription.setText("")

        val intent = Intent(this, ApprovalActivity::class.java).apply {
            putExtra("USER_TYPE", userType)
        }
        startActivity(intent)
    }

    private fun imagePickIntent() {
        Log.d(TAG, "imagePickIntent: starting image pick intent")

        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        imageActivityResultLauncher.launch(intent)
    }

    private val imageActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
        ActivityResultCallback { result ->
            if (result.resultCode == RESULT_OK) {
                Log.d(TAG, "Image Picked")
                val uri = result.data?.data

                // Convert the image URI to a File object
                val imgFile = FileHelper.uriToFile(uri!!, this)

                // Reduce the size of the image file
                val reducedImgFile = FileHelper.reduceFileImage(imgFile)

                // Update uri to point to the reduced image file
                val reducedUri = Uri.fromFile(reducedImgFile)

                // Set the picked image to your ImageView
                viewModel.setActivityImgUri(reducedUri)
                binding.ivPreview.setImageURI(reducedUri)
            } else {
                Log.d(TAG, "Image pick cancelled")
                Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show()
            }
        }
    )

    private fun setupViewModel() {
        viewModel.activityImgUri.observe(this, Observer { uri ->
            uri?.let {
                binding.ivPreview.setImageURI(it)
            }
        })
    }

    private fun setupToolbar() {
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressedDispatcher.onBackPressed()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }
}