package com.ipb.simpt.ui.dosen.approval

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.ipb.simpt.model.DataModel
import com.ipb.simpt.repository.DataRepository

class ApprovalViewModel : ViewModel() {
    private val repository = DataRepository()

    private val _items = MutableLiveData<List<DataModel>>()
    val items: LiveData<List<DataModel>> get() = _items

    private val db = FirebaseFirestore.getInstance()

    fun fetchItems(category: String) {
        repository.fetchData(category) { dataList ->
            _items.postValue(dataList)
            Log.d("ApprovalViewModel", "Fetched items: ${dataList.size}")
        }
    }

    fun fetchItemsByStatus(status: String) {
        repository.fetchDataByStatus(status) { dataList ->
            _items.postValue(dataList)
            Log.d("ApprovalViewModel", "Fetched items: ${dataList.size}")
        }
    }

    fun fetchCategoryName(category: String, id: String, callback: (String) -> Unit) {
        repository.fetchCategoryName(category, id, callback)
    }

    fun fetchUserName(uid: String, callback: (String) -> Unit) {
        repository.fetchUserName(uid, callback)
    }

    fun approveData(id: String) {
        repository.updateApprovalStatus(id, "Approved")
    }

    fun rejectData(id: String) {
        repository.updateApprovalStatus(id, "Rejected")
    }
}
