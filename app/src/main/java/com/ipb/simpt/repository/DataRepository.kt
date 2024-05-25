package com.ipb.simpt.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.ipb.simpt.model.DataModel

class DataRepository {

    private val db = FirebaseFirestore.getInstance()

    fun fetchDataByStatus(status: String, callback: (List<DataModel>) -> Unit) {
        db.collection("Data")
            .whereEqualTo("status", status)
            .get()
            .addOnSuccessListener { result ->
                val dataList = result.map { document ->
                    document.toObject(DataModel::class.java)
                }
                callback(dataList)
            }
            .addOnFailureListener { exception ->
                Log.w("DataRepository", "Error getting documents: ", exception)
                callback(emptyList())
            }
    }

    fun fetchCategoryName(category: String, id: String, callback: (String) -> Unit) {
        db.collection("Categories")
            .document(category)
            .collection("Items")
            .document(id)
            .get()
            .addOnSuccessListener { document ->
                val name = document.getString("name")
                if (name != null) {
                    callback(name)
                } else {
                    Log.w("DataRepository", "No name found in document")
                }
            }
            .addOnFailureListener { exception ->
                Log.w("DataRepository", "Error getting document: ", exception)
                callback("")
            }
    }

    fun fetchUserName(uid: String, callback: (String) -> Unit) {
        db.collection("Users")
            .document(uid)
            .get()
            .addOnSuccessListener { document ->
                val userName = document.getString("userName")
                if (userName != null) {
                    callback(userName)
                } else {
                    Log.w("DataRepository", "No user name found in document")
                }
            }
            .addOnFailureListener { exception ->
                Log.w("DataRepository", "Error getting document: ", exception)
                callback("")
            }
    }

    fun fetchData(category: String, callback: (List<DataModel>) -> Unit) {
        val query = db.collection("Data")
            .whereEqualTo(
                when (category) {
                    "Komoditas" -> "komoditasId"
                    "Penyakit" -> "penyakitId"
                    "Gejala Penyakit" -> "gejalaId"
                    else -> "unknown"
                }, category
            )

        query.get()
            .addOnSuccessListener { result ->
                val dataList = result.map { document ->
                    document.toObject(DataModel::class.java)
                }
                callback(dataList)
            }
            .addOnFailureListener { exception ->
                Log.w("DataRepository", "Error getting documents: ", exception)
                callback(emptyList())
            }
    }

    // TODO: FETCH DATA!! BUKAN FETCH CATEGORY!!!!
//    fun fetchData(category: String, callback: (List<DataModel>) -> Unit) {
//        db.collection("Data")
//            .whereEqualTo("category", category)
//            .get()
//            .addOnSuccessListener { result ->
//                val dataList = result.map { document ->
//                    document.toObject(DataModel::class.java)
//                }
//                callback(dataList)
//            }
//            .addOnFailureListener { exception ->
//                Log.w("DataRepository", "Error getting documents: ", exception)
//                callback(emptyList())
//            }
//    }

    fun fetchNames(dataModel: DataModel, callback: (DataModel) -> Unit) {
        fetchKomoditasName(dataModel) {
            fetchPenyakitName(it) {
                fetchGejalaName(it) {
                    fetchKategoriPathogenName(it) {
                        fetchPathogenName(it, callback)
                    }
                }
            }
        }
    }

    private fun fetchKomoditasName(dataModel: DataModel, callback: (DataModel) -> Unit) {
        db.collection("Categories").document("Komoditas").collection("Items")
            .document(dataModel.komoditasId)
            .get()
            .addOnSuccessListener { document ->
                dataModel.komoditasId = document.getString("name") ?: ""
                callback(dataModel)
            }
            .addOnFailureListener { exception ->
                Log.w("DataRepository", "Error getting document: ", exception)
            }
    }

    private fun fetchPenyakitName(dataModel: DataModel, callback: (DataModel) -> Unit) {
        db.collection("Categories").document("Penyakit").collection("Items")
            .document(dataModel.penyakitId)
            .get()
            .addOnSuccessListener { document ->
                dataModel.penyakitId = document.getString("name") ?: ""
                callback(dataModel)
            }
            .addOnFailureListener { exception ->
                Log.w("DataRepository", "Error getting document: ", exception)
            }
    }

    private fun fetchGejalaName(dataModel: DataModel, callback: (DataModel) -> Unit) {
        db.collection("Categories").document("Gejala Penyakit").collection("Items")
            .document(dataModel.gejalaId)
            .get()
            .addOnSuccessListener { document ->
                dataModel.gejalaId = document.getString("name") ?: ""
                callback(dataModel)
            }
            .addOnFailureListener { exception ->
                Log.w("DataRepository", "Error getting document: ", exception)
            }
    }

    private fun fetchKategoriPathogenName(dataModel: DataModel, callback: (DataModel) -> Unit) {
        db.collection("Categories").document("Kategori Pathogen")
            .get()
            .addOnSuccessListener { document ->
                val categoryCollection = when (dataModel.kategoriPathogen) {
                    "Virus" -> "Virus"
                    "Bakteri" -> "Bakteri"
                    "Cendawan" -> "Cendawan"
                    "Nematoda" -> "Nematoda"
                    "Fitoplasma" -> "Fitoplasma"
                    else -> ""
                }

                dataModel.kategoriPathogen = categoryCollection
                callback(dataModel)
            }
            .addOnFailureListener { exception ->
                Log.w("DataRepository", "Error getting document: ", exception)
            }
    }

    private fun fetchPathogenName(dataModel: DataModel, callback: (DataModel) -> Unit) {
        val categoryCollection = dataModel.kategoriPathogen
        db.collection("Categories").document("Kategori Pathogen").collection(categoryCollection)
            .document(dataModel.pathogenId)
            .get()
            .addOnSuccessListener { document ->
                dataModel.pathogenId = document.getString("name") ?: ""
                callback(dataModel)
            }
            .addOnFailureListener { exception ->
                Log.w("DataRepository", "Error getting document: ", exception)
            }
    }


    fun updateApprovalStatus(id: String, status: String) {
        db.collection("Data")
            .document(id)
            .update("status", status)
    }
}
