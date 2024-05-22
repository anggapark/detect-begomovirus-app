package com.ipb.simpt.ui.dosen.approval

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.ipb.simpt.R
import com.ipb.simpt.databinding.ActivityApprovalDetailBinding
import com.ipb.simpt.model.DataModel

class ApprovalDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityApprovalDetailBinding
    private lateinit var dataModel: DataModel

    companion object {
        private const val TAG = "ApprovalDetailActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityApprovalDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()
        setupToolbar()


        dataModel = intent.getSerializableExtra("dataModel") as DataModel
        displayDataDetails()

        binding.btnApprove.setOnClickListener {
            updateSubmissionStatus("Approved")
        }

        binding.btnReject.setOnClickListener {
            updateSubmissionStatus("Rejected")
        }
    }

    private fun displayDataDetails() {
        binding.apply {
            tvKomoditas.text = dataModel.komoditasId
            tvPenyakit.text = dataModel.penyakitId
            tvPathogen.text = dataModel.pathogenId
            tvGejala.text = dataModel.gejalaId
            tvKategoriPathogen.text = dataModel.kategoriPathogen
            tvDataset.text = dataModel.dataset
            tvDeskripsi.text = dataModel.deskripsi
        }
    }


    private fun updateSubmissionStatus(status: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("Data")
            .document(dataModel.id)
            .update("status", status)
            .addOnSuccessListener {
                Log.d(TAG, "updateSubmissionStatus: Submission status updated to $status")
                finish()
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "updateSubmissionStatus: Failed to update status due to ${e.message}")
            }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
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
}