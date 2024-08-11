package com.ipb.simpt.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.ipb.simpt.model.DataModel
import com.ipb.simpt.model.FilterModel
import com.ipb.simpt.utils.FileHelper
import kotlinx.coroutines.tasks.await

class DataRepository {

    private val db = FirebaseFirestore.getInstance()

    // Caches for storing fetched names
    private val komoditasCache = mutableMapOf<String, String>()
    private val penyakitCache = mutableMapOf<String, String>()
    private val gejalaCache = mutableMapOf<String, String>()
    private val pathogenCache = mutableMapOf<String, String>()
    private val userNameCache = mutableMapOf<String, String>()
    private val userNimCache = mutableMapOf<String, String>()


    fun fetchInitialApprovedItems(
        pageSize: Int,
        onComplete: (List<DataModel>, Long) -> Unit,
        onError: (String) -> Unit
    ) {
        db.collection("Data")
            .whereEqualTo("status", "Approved")
            .orderBy("timestamp")
            .limit(pageSize.toLong())
            .get()
            .addOnSuccessListener { snapshot ->
                val items = snapshot.toObjects(DataModel::class.java)
                val lastVisibleItemTimestamp = items.lastOrNull()?.timestamp ?: 0L
                onComplete(items, lastVisibleItemTimestamp)
            }
            .addOnFailureListener { exception ->
                onError(exception.message ?: "Error fetching items")
            }
    }

    fun fetchMoreApprovedItems(
        lastTimestamp: Long,
        pageSize: Int,
        onComplete: (List<DataModel>, Long) -> Unit,
        onError: (String) -> Unit
    ) {
        db.collection("Data")
            .whereEqualTo("status", "Approved")
            .orderBy("timestamp")
            .startAfter(lastTimestamp)
            .limit(pageSize.toLong())
            .get()
            .addOnSuccessListener { snapshot ->
                val items = snapshot.toObjects(DataModel::class.java)
                val lastVisibleItemTimestamp = items.lastOrNull()?.timestamp ?: 0L
                onComplete(items, lastVisibleItemTimestamp)
            }
            .addOnFailureListener { exception ->
                onError(exception.message ?: "Error fetching items")
            }
    }


    fun fetchApprovedItemsByPage(
        pageSize: Int, lastItem: DataModel?, onComplete: (List<DataModel>) -> Unit, onError: (String) -> Unit
    ) {
        val query = db.collection("Data")
            .whereEqualTo("status", "Approved")
            .orderBy("timestamp")
            .limit(pageSize.toLong())

        val finalQuery = if (lastItem != null) {
            query.startAfter(lastItem.timestamp)
        } else {
            query
        }

        finalQuery.get()
            .addOnSuccessListener { snapshot ->
                val items = snapshot.toObjects(DataModel::class.java)
                onComplete(items)
            }
            .addOnFailureListener { exception ->
                onError(exception.message ?: "Error fetching items")
            }
    }

    fun fetchFilteredItems(filterModel: FilterModel, callback: (List<DataModel>) -> Unit) {
        var query: Query = db.collection("Data").orderBy("timestamp")

        if (filterModel.komoditasId != null) {
            query = query.whereEqualTo("komoditasId", filterModel.komoditasId)
        }
        if (filterModel.penyakitId != null) {
            query = query.whereEqualTo("penyakitId", filterModel.penyakitId)
        }
        if (filterModel.gejalaId != null) {
            query = query.whereEqualTo("gejalaId", filterModel.gejalaId)
        }
        if (filterModel.pathogenId != null) {
            query = query.whereEqualTo("pathogenId", filterModel.pathogenId)
        }
        if (filterModel.kategoriPathogen != null) {
            query = query.whereEqualTo("kategoriPathogen", filterModel.kategoriPathogen)
        }
        if (!filterModel.date.isNullOrEmpty()) {
            val startOfDay = FileHelper.getStartOfDayInMillis(filterModel.date)
            val endOfDay = FileHelper.getEndOfDayInMillis(filterModel.date)
            query = query.whereGreaterThanOrEqualTo("timestamp", startOfDay)
                .whereLessThanOrEqualTo("timestamp", endOfDay)
        }

        query.get().addOnSuccessListener { documents ->
            val items = mutableListOf<DataModel>()
            for (document in documents) {
                val item = document.toObject(DataModel::class.java)
                items.add(item)
            }
            callback(items)
        }.addOnFailureListener { exception ->
            Log.w("DataRepository", "Error getting documents: ", exception)
            callback(emptyList())
        }
    }

    fun fetchDataByCategory(
        categoryName: String,
        itemId: String,
        categoryValue: String?,
        callback: (List<DataModel>) -> Unit
    ) {
        val fieldName = when (categoryName) {
            "Komoditas" -> "komoditasId"
            "Penyakit" -> "penyakitId"
            "Gejala Penyakit" -> "gejalaId"
            "Kategori Pathogen" -> "pathogenId"
            else -> throw IllegalArgumentException("Invalid category name")
        }

        val query = if (categoryName == "Kategori Pathogen" && categoryValue != null) {
            db.collection("Data")
                .whereEqualTo("kategoriPathogen", categoryValue)
                .whereEqualTo(fieldName, itemId)
        } else {
            db.collection("Data")
                .whereEqualTo(fieldName, itemId)
        }

        query.get()
            .addOnSuccessListener { result ->
                val dataList = result.map { document ->
                    document.toObject(DataModel::class.java)
                }
                Log.d(
                    "DataRepository",
                    "fetchDataByCategory onSuccess called with ${dataList.size} items"
                )
                callback(dataList)
            }
            .addOnFailureListener { exception ->
                Log.w("DataRepository", "Error getting documents: ", exception)
                callback(emptyList())
            }
    }

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

    fun fetchDataByStatusAndUserId(
        status: String,
        userId: String,
        callback: (List<DataModel>) -> Unit
    ) {
        db.collection("Data")
            .whereEqualTo("status", status)
            .whereEqualTo("uid", userId)
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

    fun fetchDataByUserId(userId: String, callback: (List<DataModel>) -> Unit) {
        db.collection("Data")
            .whereEqualTo("uid", userId)
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


    fun fetchDataResponse(itemId: String, callback: (String) -> Unit) {
        db.collection("Data")
            .document(itemId)
            .get()
            .addOnSuccessListener { document ->
                val comment = document.getString("comment") ?: ""
                callback(comment)
            }
            .addOnFailureListener { exception ->
                Log.w("DataRepository", "Error getting document: ", exception)
                callback("")
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

    fun fetchPathogenName(categoryValue: String?, itemId: String, callback: (String) -> Unit) {
        if (categoryValue != null) {
            db.collection("Categories")
                .document("Kategori Pathogen")
                .collection(categoryValue)
                .document(itemId)
                .get()
                .addOnSuccessListener { document ->
                    val name = document.getString("name") ?: ""
                    callback(name)
                }
                .addOnFailureListener { exception ->
                    Log.w("DataRepository", "Error getting document: ", exception)
                    callback("")
                }
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


    fun updateApprovalStatus(id: String, status: String, comment: String) {
        val updateMap = mapOf(
            "status" to status,
            "comment" to comment
        )

        db.collection("Data")
            .document(id)
            .update(updateMap)
    }


    suspend fun deleteData(itemId: String): Boolean {
        return try {
            db.collection("Data").document(itemId).delete().await()
            Log.d("DataRepository", "DocumentSnapshot successfully deleted!")
            true
        } catch (e: Exception) {
            Log.w("DataRepository", "Error deleting document", e)
            false
        }
    }
}
