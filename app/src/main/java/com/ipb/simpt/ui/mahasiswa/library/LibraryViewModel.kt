package com.ipb.simpt.ui.mahasiswa.library

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ipb.simpt.model.DataModel
import com.ipb.simpt.model.UserModel
import com.ipb.simpt.repository.DataRepository
import com.ipb.simpt.repository.UserRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

class LibraryViewModel : ViewModel() {

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


    fun fetchApprovedItems() {
        _isLoading.value = true
        coroutineScope.launch {
            try {
                withTimeout(5000L) {
                    repository.fetchDataByStatus("Approved") { dataList ->
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

    fun fetchDataByStatusAndUserId(userId: String) {
        _isLoading.value = true
        coroutineScope.launch {
            try {
                withTimeout(5000L) {
                    val dataList = mutableListOf<DataModel>()
                    repository.fetchDataByStatusAndUserId("Approved", userId) { fetchedList ->
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

    fun fetchItemsByUserId(userId: String) {
        _isLoading.value = true
        coroutineScope.launch {
            try {
                withTimeout(5000L) {
                    repository.fetchDataByUserId(userId) { dataList ->
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

    private fun checkEmptyState(items: List<DataModel>) {
        if (items.isEmpty()) {
            _error.postValue("No items found")
        }
    }

    fun fetchUserName(uid: String, callback: (String) -> Unit) {
        repository.fetchUserName(uid, callback)
    }

    fun fetchItemDetails(id: String, callback: (DataModel) -> Unit) {
        repository.fetchItemDetail(id) { dataModel ->
            callback(dataModel)
        }
    }

    // Method to fetch and cache category names and user details
    fun fetchAndCacheNames(dataModel: DataModel, callback: () -> Unit) {
        repository.fetchCategoryName("Komoditas", dataModel.komoditasId) { komoditasName ->
            dataModel.komoditasName = komoditasName
            repository.fetchCategoryName("Penyakit", dataModel.penyakitId) { penyakitName ->
                dataModel.penyakitName = penyakitName
                repository.fetchCategoryName("Gejala Penyakit", dataModel.gejalaId) { gejalaName ->
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

    fun fetchUserDetails(uid: String) {
        userRepository.fetchUserDetails(uid) { user ->
            user?.let {
                _user.value = it
            } ?: run {
                Log.w("ProfileViewModel", "User not found")
            }
        }
    }

    fun errorHandled() {
        _error.postValue(null)
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel() // Cancel all coroutines when ViewModel is cleared
    }
}