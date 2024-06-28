package com.ipb.simpt.ui.dosen.approval

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ipb.simpt.model.DataModel
import com.ipb.simpt.model.UserModel
import com.ipb.simpt.repository.DataRepository
import com.ipb.simpt.repository.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

class ApprovalViewModel : ViewModel() {
    private val repository = DataRepository()
    private val userRepository = UserRepository()

    private val _items = MutableLiveData<List<DataModel>>()
    val items: LiveData<List<DataModel>> get() = _items

    private val _user = MutableLiveData<UserModel>()
    val user: LiveData<UserModel> get() = _user

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> get() = _error

    private val viewModelJob = Job()
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    // Fetch and cache names for filtering
    fun fetchItemsByStatus(status: String) {
        _isLoading.value = true
        coroutineScope.launch {
            try {
                withTimeout(5000L) {
                    repository.fetchDataByStatus(status) { dataList ->
                        val updatedDataList = mutableListOf<DataModel>()
                        dataList.forEach { dataModel ->
                            repository.fetchAndCacheNames(dataModel) {
                                repository.fetchAndCacheUserDetails(dataModel) {
                                    updatedDataList.add(dataModel)
                                    if (updatedDataList.size == dataList.size) {
                                        _items.postValue(updatedDataList)
                                        _isLoading.postValue(false)
                                        checkEmptyState(updatedDataList)
                                    }
                                }
                            }
                        }
                        if (dataList.isEmpty()) {
                            _isLoading.postValue(false)
                            checkEmptyState(dataList)
                        }
                    }
                }
            } catch (e: TimeoutCancellationException) {
                _isLoading.postValue(false)
                _error.postValue("Request timed out")
            } catch (e: Exception) {
                _isLoading.postValue(false)
                _error.postValue(e.message)
            }
        }
    }

    // Method to fetch items by user ID and status
    fun fetchItemsByUserAndStatus(userId: String, status: String) {
        _isLoading.value = true
        coroutineScope.launch {
            try {
                withTimeout(5000L) {
                    val dataList = mutableListOf<DataModel>()
                    repository.fetchDataByStatusAndUserId(status, userId) { fetchedList ->
                        fetchedList.forEach { dataModel ->
                            repository.fetchAndCacheNames(dataModel) {
                                repository.fetchAndCacheUserDetails(dataModel) {
                                    dataList.add(dataModel)
                                    if (dataList.size == fetchedList.size) {
                                        _items.postValue(dataList)
                                        _isLoading.postValue(false)
                                        checkEmptyState(dataList)
                                    }
                                }
                            }
                        }
                        if (fetchedList.isEmpty()) {
                            _isLoading.postValue(false)
                            checkEmptyState(fetchedList)
                        }
                    }
                }
            } catch (e: TimeoutCancellationException) {
                _isLoading.postValue(false)
                _error.postValue("Request timed out")
            } catch (e: Exception) {
                _isLoading.postValue(false)
                _error.postValue(e.message)
            }
        }
    }

    private fun checkEmptyState(items: List<DataModel>) {
        if (items.isEmpty()) {
            // Handle empty state
            // Optionally, you can show a message or handle visibility here
            // Example:
            _error.postValue("No items found")
        }
    }


    fun deleteData(itemId: String, callback: (Boolean) -> Unit) {
        viewModelScope.launch {
            val result = repository.deleteData(itemId)
            callback(result)
        }
    }

    fun fetchCategoryName(category: String, id: String, callback: (String) -> Unit) {
        coroutineScope.launch {
            repository.fetchCategoryName(category, id, callback)
        }
    }

    fun fetchDataResponse(itemId: String, callback: (String) -> Unit) {
        coroutineScope.launch {
            repository.fetchDataResponse(itemId, callback)
        }
    }

    fun fetchUserName(uid: String, callback: (String) -> Unit) {
        coroutineScope.launch {
            repository.fetchUserName(uid, callback)
        }
    }

    fun fetchItemDetails(id: String, callback: (DataModel) -> Unit) {
        coroutineScope.launch {
            repository.fetchItemDetail(id) { dataModel ->
                callback(dataModel)
            }
        }
    }

    // Method to fetch and cache category names and user details
    fun fetchAndCacheNames(dataModel: DataModel, callback: () -> Unit) {
        coroutineScope.launch {
            repository.fetchCategoryName("Komoditas", dataModel.komoditasId) { komoditasName ->
                dataModel.komoditasName = komoditasName
                repository.fetchCategoryName("Penyakit", dataModel.penyakitId) { penyakitName ->
                    dataModel.penyakitName = penyakitName
                    repository.fetchCategoryName(
                        "Gejala Penyakit",
                        dataModel.gejalaId
                    ) { gejalaName ->
                        dataModel.gejalaName = gejalaName
                        repository.fetchKategoriPathogen(dataModel) { updatedDataModel ->
                            repository.fetchPathogen(updatedDataModel) {
                                callback()
                            }
                        }
                    }
                }
            }
        }
    }

    fun approveData(itemId: String, comment: String) {
        coroutineScope.launch {
            repository.updateApprovalStatus(itemId, "Approved", comment)
        }
    }

    fun rejectData(itemId: String, comment: String) {
        coroutineScope.launch {
            repository.updateApprovalStatus(itemId, "Rejected", comment)
        }
    }

    fun fetchUserDetails(uid: String) {
        coroutineScope.launch {
            userRepository.fetchUserDetails(uid) { user ->
                user?.let {
                    _user.value = it
                } ?: run {
                    Log.w("ProfileViewModel", "User not found")
                }
            }
        }
    }

    public override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel() // Cancel all coroutines when ViewModel is cleared
    }
}
