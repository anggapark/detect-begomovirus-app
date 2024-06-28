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
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.Task
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

    private var itemId: String? = null
    private var userType: String = "user"
    private var imgUri: Uri? = null
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

        setupAction()
    }

    private fun setupUI() {
        if (itemId != null) {
            // Edit mode
            supportActionBar?.setTitle(R.string.title_edit_data)
            binding.btnGallery.text = getString(R.string.hint_ganti_gambar)
            binding.btnSave.visibility = View.VISIBLE
            binding.btnSubmit.visibility = View.GONE
        } else {
            // Add mode
            supportActionBar?.setTitle(R.string.home_add)
            binding.btnGallery.text = getString(R.string.hint_gambar)
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

            btnGallery.setOnClickListener {
                imagePickIntent()
            }

            btnSubmit.setOnClickListener {
                btnSubmit.isEnabled = false // Disable the button when clicked
                addData(
                    selectedKomoditasId ?: "",
                    selectedPenyakitId ?: "",
                    selectedPathogenId ?: "",
                    selectedGejalaId ?: ""
                )
            }

            btnSave.setOnClickListener {
                btnSave.isEnabled = false // Disable the button when clicked
                updateData(
                    selectedKomoditasId ?: "",
                    selectedPenyakitId ?: "",
                    selectedPathogenId ?: "",
                    selectedGejalaId ?: ""
                )
            }
        }
    }

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

        when {
            komoditasId.isEmpty() -> toast("Pick Komoditas")
            penyakitId.isEmpty() -> toast("Pick Penyakit")
            kategoriPathogen.isEmpty() -> toast("Pick Kategori Pathogen")
            pathogenId.isEmpty() -> toast("Pick Pathogen")
            gejalaId.isEmpty() -> toast("Pick Gejala Penyakit")
            dataset.isEmpty() -> toast("Enter Dataset")
            deskripsi.isEmpty() -> toast("Enter Description")
            imgUri == null -> toast("Upload Image")
            else -> {
                // Data validated, begin upload
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

        binding.btnSubmit.isEnabled = true
    }

    private fun updateData(
        komoditasId: String,
        penyakitId: String,
        pathogenId: String,
        gejalaId: String
    ) {
        Log.d(TAG, "updateData: validating data")

        val kategoriPathogen = binding.tvKategoriPathogen.text.toString().trim()
        val dataset = binding.edAddDataset.text.toString().trim()
        val deskripsi = binding.edAddDescription.text.toString().trim()

        when {
            komoditasId.isEmpty() -> toast("Pick Komoditas")
            penyakitId.isEmpty() -> toast("Pick Penyakit")
            kategoriPathogen.isEmpty() -> toast("Pick Kategori Pathogen")
            pathogenId.isEmpty() -> toast("Pick Pathogen")
            gejalaId.isEmpty() -> toast("Pick Gejala Penyakit")
            dataset.isEmpty() -> toast("Enter Dataset")
            deskripsi.isEmpty() -> toast("Enter Description")
            else -> {
                // Data validated, begin update
                uploadImgToStorage(
                    komoditasId,
                    penyakitId,
                    kategoriPathogen,
                    pathogenId,
                    gejalaId,
                    dataset,
                    deskripsi,
                    true
                )
            }
        }

        binding.btnSave.isEnabled = true
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
        // Upload image to firebase storage
        Log.d(TAG, "uploadImgToStorage: uploading to storage")

        showLoading(true)

        // timestamp
        val timestamp = System.currentTimeMillis()

        // path of img in firebase storage
        val filePathAndName = "Images/$timestamp"

        // storage reference
        val storageReference = FirebaseStorage.getInstance().getReference(filePathAndName)


        if (imgUri != null) {
            storageReference.putFile(imgUri!!)
                .addOnSuccessListener { taskSnapshot ->
                    val uriTask: Task<Uri> = taskSnapshot.storage.downloadUrl
                    uriTask.addOnSuccessListener { uri ->
                        val uploadedImgUrl = uri.toString()
                        if (isUpdate) {
                            uploadDataInfoToDb(
                                uploadedImgUrl,
                                timestamp,
                                komoditasId,
                                penyakitId,
                                kategoriPathogen,
                                pathogenId,
                                gejalaId,
                                dataset,
                                deskripsi,
                                true
                            )
                        } else {
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
                    }
                }
                .addOnFailureListener { e ->
                    showLoading(false)
                    toast("Failed to upload due to ${e.message}")
                    binding.btnSubmit.isEnabled = true // enable the button if upload fails
                    binding.btnSave.isEnabled = true // enable the button if upload fails
                }
        } else {
            if (isUpdate) {
                // If no image is selected but it's an update operation, keep the old image URL
                uploadDataInfoToDb(
                    "", // Pass an empty string to indicate no new image URL
                    timestamp,
                    komoditasId,
                    penyakitId,
                    kategoriPathogen,
                    pathogenId,
                    gejalaId,
                    dataset,
                    deskripsi,
                    true
                )
            }
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
        deskripsi: String,
        isUpdate: Boolean = false
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

        val db = FirebaseFirestore.getInstance()

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
        imgUri = null
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
                imgUri = result.data?.data

                // Convert the image URI to a File object
                val imgFile = FileHelper.uriToFile(imgUri!!, this)

                // Reduce the size of the image file
                val reducedImgFile = FileHelper.reduceFileImage(imgFile)

                // Update imgUri to point to the reduced image file
                imgUri = Uri.fromFile(reducedImgFile)

                // Set the picked image to your ImageView
                binding.ivPreview.setImageURI(imgUri)
            } else {
                Log.d(TAG, "Image pick cancelled")
                Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show()
            }
        }
    )

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