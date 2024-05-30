package com.ipb.simpt.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.ipb.simpt.model.DataModel

class DataRepository {

    private val db = FirebaseFirestore.getInstance()

    // Caches for storing fetched names
    private val komoditasCache = mutableMapOf<String, String>()
    private val penyakitCache = mutableMapOf<String, String>()
    private val gejalaCache = mutableMapOf<String, String>()
    private val pathogenCache = mutableMapOf<String, String>()
    private val userNameCache = mutableMapOf<String, String>()
    private val userNimCache = mutableMapOf<String, String>()

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
        val cache = when (category) {
            "Komoditas" -> komoditasCache
            "Penyakit" -> penyakitCache
            "Gejala Penyakit" -> gejalaCache
            else -> null
        }

        cache?.get(id)?.let {
            callback(it)
        } ?: db.collection("Categories")
            .document(category)
            .collection("Items")
            .document(id)
            .get()
            .addOnSuccessListener { document ->
                val name = document.getString("name") ?: ""
                cache?.put(id, name)
                callback(name)
            }
            .addOnFailureListener { exception ->
                Log.w("DataRepository", "Error getting document: ", exception)
                callback("")
            }
    }

    fun fetchKategoriPathogen(dataModel: DataModel, callback: (DataModel) -> Unit) {
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

    fun fetchPathogen(dataModel: DataModel, callback: (DataModel) -> Unit) {
        val categoryCollection = dataModel.kategoriPathogen
        db.collection("Categories")
            .document("Kategori Pathogen")
            .collection(categoryCollection)
            .document(dataModel.pathogenId)
            .get()
            .addOnSuccessListener { document ->
                dataModel.pathogenName = document.getString("name") ?: ""
                pathogenCache[dataModel.pathogenId] = dataModel.pathogenName ?: ""
                callback(dataModel)
            }
            .addOnFailureListener { exception ->
                Log.w("DataRepository", "Error getting document: ", exception)
                callback(dataModel)
            }
    }

    fun fetchAndCacheNames(dataModel: DataModel, callback: () -> Unit) {
        fetchCategoryName("Komoditas", dataModel.komoditasId) { komoditasName ->
            dataModel.komoditasName = komoditasName
            fetchCategoryName("Penyakit", dataModel.penyakitId) { penyakitName ->
                dataModel.penyakitName = penyakitName
                fetchCategoryName("Gejala Penyakit", dataModel.gejalaId) { gejalaName ->
                    dataModel.gejalaName = gejalaName
                    fetchKategoriPathogen(dataModel) { updatedDataModel ->
                        fetchPathogen(updatedDataModel) {
                            fetchAndCacheUserDetails(dataModel) {
                                callback()
                            }
                        }
                    }
                }
            }
        }
    }


    fun fetchAndCacheUserDetails(dataModel: DataModel, callback: () -> Unit) {
        fetchUserName(dataModel.uid) { userName ->
            dataModel.userName = userName
            fetchUserNim(dataModel.uid) { userNim ->
                dataModel.userNim = userNim
                callback()
            }
        }
    }

    fun fetchUserName(uid: String, callback: (String) -> Unit) {
        userNameCache[uid]?.let {
            callback(it)
        } ?: db.collection("Users")
            .document(uid)
            .get()
            .addOnSuccessListener { document ->
                val userName = document.getString("userName") ?: ""
                userNameCache[uid] = userName
                callback(userName)
            }
            .addOnFailureListener { exception ->
                Log.w("DataRepository", "Error getting document: ", exception)
                callback("")
            }
    }

    fun fetchUserNim(uid: String, callback: (String) -> Unit) {
        userNimCache[uid]?.let {
            callback(it)
        } ?: db.collection("Users")
            .document(uid)
            .get()
            .addOnSuccessListener { document ->
                val userNim = document.getString("userNim") ?: ""
                userNimCache[uid] = userNim
                callback(userNim)
            }
            .addOnFailureListener { exception ->
                Log.w("DataRepository", "Error getting document: ", exception)
                callback("")
            }
    }

    fun fetchItemDetail(id: String, callback: (DataModel) -> Unit) {
        db.collection("Data").document(id).get()
            .addOnSuccessListener { document ->
                val dataModel = document.toObject(DataModel::class.java) ?: DataModel()
                fetchAndCacheNames(dataModel) {
                    fetchAndCacheUserDetails(dataModel) {
                        callback(dataModel)
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.w("DataRepository", "Error getting document: ", exception)
                callback(DataModel())
            }
    }


    fun updateApprovalStatus(id: String, status: String) {
        db.collection("Data")
            .document(id)
            .update("status", status)
    }
}
