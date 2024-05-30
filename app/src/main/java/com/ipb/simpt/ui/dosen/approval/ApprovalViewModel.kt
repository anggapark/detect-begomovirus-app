package com.ipb.simpt.ui.dosen.approval

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ipb.simpt.model.DataModel
import com.ipb.simpt.repository.DataRepository

class ApprovalViewModel : ViewModel() {
    private val repository = DataRepository()

    private val _items = MutableLiveData<List<DataModel>>()
    val items: LiveData<List<DataModel>> get() = _items

    // Fetch and cache names for filtering
    fun fetchItemsByStatus(status: String) {
        repository.fetchDataByStatus(status) { dataList ->
            val updatedDataList = mutableListOf<DataModel>()
            dataList.forEach { dataModel ->
                repository.fetchAndCacheNames(dataModel) {
                    repository.fetchAndCacheUserDetails(dataModel) {
                        updatedDataList.add(dataModel)
                        if (updatedDataList.size == dataList.size) {
                            _items.postValue(updatedDataList)
                        }
                    }
                }
            }
        }
    }

    fun fetchCategoryName(category: String, id: String, callback: (String) -> Unit) {
        repository.fetchCategoryName(category, id, callback)
    }

    fun fetchUserName(uid: String, callback: (String) -> Unit) {
        repository.fetchUserName(uid, callback)
    }
}
