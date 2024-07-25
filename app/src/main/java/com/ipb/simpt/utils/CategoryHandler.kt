package com.ipb.simpt.utils

import android.app.AlertDialog
import android.content.Context
import android.view.View
import android.widget.ProgressBar
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot

class CategoryHandler(private val context: Context, private val progressBar: ProgressBar) {

    private val db = FirebaseFirestore.getInstance()


    // load each categories
    fun loadKomoditasCategory(): Task<QuerySnapshot> {
        showLoading(true)
        return db.collection("Categories").document("Komoditas").collection("Items").get().addOnCompleteListener {
            showLoading(false)
        }
    }

    fun loadPenyakitCategory(): Task<QuerySnapshot> {
        showLoading(true)
        return db.collection("Categories").document("Penyakit").collection("Items").get().addOnCompleteListener {
            showLoading(false)
        }
    }

    fun loadGejalaCategory(): Task<QuerySnapshot> {
        showLoading(true)
        return db.collection("Categories").document("Gejala Penyakit").collection("Items").get().addOnCompleteListener {
            showLoading(false)
        }
    }

    fun loadPathogenCategory(pathogen: String): Task<QuerySnapshot> {
        showLoading(true)
        return db.collection("Categories").document("Kategori Pathogen").collection(pathogen).get().addOnCompleteListener {
            showLoading(false)
        }
    }


    // show pick dialog for each categories
    fun showKomoditasDialog(onItemSelected: (String, String) -> Unit) {
        loadKomoditasCategory().addOnSuccessListener { documents ->
            val items = documents.map { it.id to it.getString("name") }.toTypedArray()
            showCategoryDialog("Pilih Komoditas", items.map { it.second }.toTypedArray()) { selectedKomoditas ->
                val selectedId = items.find { it.second == selectedKomoditas }?.first
                if (selectedId != null) {
                    onItemSelected(selectedId, selectedKomoditas)
                }
            }
        }
    }

    fun showPenyakitDialog(onItemSelected: (String, String) -> Unit) {
        loadPenyakitCategory().addOnSuccessListener { documents ->
            val items = documents.map { it.id to it.getString("name") }.toTypedArray()
            showCategoryDialog("Pilih Penyakit", items.map { it.second }.toTypedArray()) { selectedPenyakit ->
                val selectedId = items.find { it.second == selectedPenyakit }?.first
                if (selectedId != null) {
                    onItemSelected(selectedId, selectedPenyakit)
                }
            }
        }
    }

    fun showGejalaDialog(onItemSelected: (String, String) -> Unit) {
        loadGejalaCategory().addOnSuccessListener { documents ->
            val items = documents.map { it.id to it.getString("name") }.toTypedArray()
            showCategoryDialog("Pilih Gejala Penyakit", items.map { it.second }.toTypedArray()) { selectedGejala ->
                val selectedId = items.find { it.second == selectedGejala }?.first
                if (selectedId != null) {
                    onItemSelected(selectedId, selectedGejala)
                }
            }
        }
    }

    fun showKategoriPathogenDialog(onItemSelected: (String) -> Unit) {
        val items: Array<String?> = arrayOf("Virus", "Bakteri", "Cendawan", "Nematoda", "Fitoplasma")
        showCategoryDialog("Pilih Kategori Pathogen", items, onItemSelected)
    }

    fun showPathogenDialog(pathogen: String, onItemSelected: (String, String) -> Unit) {
        loadPathogenCategory(pathogen).addOnSuccessListener { documents ->
            val items = documents.map { it.id to it.getString("name") }.toTypedArray()
            showCategoryDialog("Pilih $pathogen", items.map { it.second }.toTypedArray()) { selectedPathogen ->
                val selectedId = items.find { it.second == selectedPathogen }?.first
                if (selectedId != null) {
                    onItemSelected(selectedId, selectedPathogen)
                }
            }
        }
    }

    private fun showCategoryDialog(
        title: String,
        items: Array<String?>,
        onItemSelected: (String) -> Unit
    ) {
        AlertDialog.Builder(context)
            .setTitle(title)
            .setItems(items) { _, which ->
                items[which]?.let { onItemSelected(it) }
            }
            .show()
    }

    private fun showLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }
}
