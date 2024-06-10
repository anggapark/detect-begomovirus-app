package com.ipb.simpt.ui.dosen.library.detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ipb.simpt.repository.CategoryRepository

class DosenCategoryViewModel : ViewModel() {

    private val categoryRepository = CategoryRepository()

    private val _updateResult = MutableLiveData<Result<String>>()
    val updateResult: LiveData<Result<String>> = _updateResult

    private val _deleteResult = MutableLiveData<Result<String>>()
    val deleteResult: LiveData<Result<String>> = _deleteResult

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
}