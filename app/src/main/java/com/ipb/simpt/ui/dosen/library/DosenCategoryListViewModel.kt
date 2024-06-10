package com.ipb.simpt.ui.dosen.library

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ipb.simpt.model.CategoryModel
import com.ipb.simpt.repository.CategoryRepository

class DosenCategoryListViewModel : ViewModel() {

    private val repository = CategoryRepository()
    private val _categoryData = MutableLiveData<List<CategoryModel>>()
    val categoryData: LiveData<List<CategoryModel>> get() = _categoryData

    fun fetchCategoryData(categoryName: String, categoryValue: String?) {
        repository.fetchDataByCategory(categoryName, categoryValue) { data ->
            _categoryData.postValue(data)
        }
    }
}