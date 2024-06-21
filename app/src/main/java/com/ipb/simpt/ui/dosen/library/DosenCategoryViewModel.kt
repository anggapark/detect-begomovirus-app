package com.ipb.simpt.ui.dosen.library

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ipb.simpt.model.CategoryModel
import com.ipb.simpt.model.DataModel
import com.ipb.simpt.repository.CategoryRepository
import com.ipb.simpt.repository.DataRepository

class DosenCategoryViewModel : ViewModel() {

    private val categoryRepository = CategoryRepository()
    private val dataRepository = DataRepository()

    private val _updateResult = MutableLiveData<Result<String>>()
    val updateResult: LiveData<Result<String>> = _updateResult

    private val _deleteResult = MutableLiveData<Result<String>>()
    val deleteResult: LiveData<Result<String>> = _deleteResult

    private val _categoryData = MutableLiveData<List<DataModel>>()
    val categoryData: LiveData<List<DataModel>> get() = _categoryData

    private val _category = MutableLiveData<List<CategoryModel>>()
    val category: LiveData<List<CategoryModel>> get() = _category

    fun updateCategoryName(categoryName: String, categoryValue: String, itemId: String, updatedName: String) {
        categoryRepository.updateCategoryName(categoryName, categoryValue, itemId, updatedName, {
            _updateResult.value = Result.success("Category name updated successfully")
        }, {
            _updateResult.postValue(Result.failure(Exception("Update failed")))
        })
    }

    fun deleteCategory(categoryName: String, categoryValue: String, itemId: String) {
        categoryRepository.deleteCategory(categoryName, categoryValue, itemId, {
            _deleteResult.value = Result.success("Category deleted successfully")
        }, {
            _deleteResult.value = Result.failure(it)
        })
    }

    fun fetchCategoryData(categoryName: String, categoryValue: String?) {
        categoryRepository.fetchDataByCategory(categoryName, categoryValue) { data ->
            _category.postValue(data)
        }
    }

    fun fetchDataByCategory(categoryName: String, itemId: String, categoryValue: String?) {
        dataRepository.fetchDataByCategory(categoryName, itemId, categoryValue) { dataList ->
            _categoryData.postValue(dataList)
        }
    }

    fun fetchCategoryName(category: String, id: String, callback: (String) -> Unit) {
        dataRepository.fetchCategoryName(category, id, callback)
    }

    fun fetchPathogenName(categoryValue: String?, itemId: String, callback: (String) -> Unit) {
        dataRepository.fetchPathogenName(categoryValue, itemId, callback)
    }

    fun fetchUserName(uid: String, callback: (String) -> Unit) {
        dataRepository.fetchUserName(uid, callback)
    }
}