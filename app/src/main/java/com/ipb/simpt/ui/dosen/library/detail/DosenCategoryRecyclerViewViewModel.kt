package com.ipb.simpt.ui.dosen.library.detail

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ipb.simpt.model.DataModel
import com.ipb.simpt.repository.DataRepository

class DosenCategoryRecyclerViewViewModel : ViewModel() {
    private val repository = DataRepository()

    private val _categoryData = MutableLiveData<List<DataModel>>()
    val categoryData: LiveData<List<DataModel>> get() = _categoryData

    fun fetchDataByCategory(categoryName: String, itemId: String, categoryValue: String?) {
        repository.fetchDataByCategory(categoryName, itemId, categoryValue) { dataList ->
            _categoryData.postValue(dataList)
        }
    }

    fun fetchCategoryName(category: String, id: String, callback: (String) -> Unit) {
        repository.fetchCategoryName(category, id, callback)
    }

    fun fetchPathogenName(categoryValue: String?, itemId: String, callback: (String) -> Unit) {
        repository.fetchPathogenName(categoryValue, itemId, callback)
    }

    fun fetchUserName(uid: String, callback: (String) -> Unit) {
        repository.fetchUserName(uid, callback)
    }
}