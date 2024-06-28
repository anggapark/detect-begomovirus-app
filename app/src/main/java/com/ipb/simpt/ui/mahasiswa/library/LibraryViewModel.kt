package com.ipb.simpt.ui.mahasiswa.library

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ipb.simpt.model.DataModel
import com.ipb.simpt.model.UserModel
import com.ipb.simpt.repository.DataRepository
import com.ipb.simpt.repository.UserRepository

class LibraryViewModel : ViewModel() {

    private val repository = DataRepository()
    private val userRepository = UserRepository()

    private val _items = MutableLiveData<List<DataModel>>()
    val items: LiveData<List<DataModel>> get() = _items

    private val _user = MutableLiveData<UserModel>()
    val user: LiveData<UserModel> get() = _user

    fun fetchApprovedItems() {
        Log.d("LibraryViewModel", "Fetching approved items")
        repository.fetchDataByStatus("Approved") { dataList ->
            val updatedDataList = mutableListOf<DataModel>()
            dataList.forEach { dataModel ->
                repository.fetchAndCacheNames(dataModel) {
                    repository.fetchAndCacheUserDetails(dataModel) {
                        updatedDataList.add(dataModel)
                        if (updatedDataList.size == dataList.size) {
                            Log.d("LibraryViewModel", "Fetched items: $updatedDataList")
                            _items.postValue(updatedDataList)
                        }
                    }
                }
            }
        }
    }

    fun fetchDataByStatusAndUserId(userId: String) {
        Log.d("LibraryViewModel", "Fetching approved items for user $userId")
        repository.fetchDataByStatusAndUserId("Approved", userId) { dataList ->
            val updatedDataList = mutableListOf<DataModel>()
            dataList.forEach { dataModel ->
                repository.fetchAndCacheNames(dataModel) {
                    repository.fetchAndCacheUserDetails(dataModel) {
                        updatedDataList.add(dataModel)
                        if (updatedDataList.size == dataList.size) {
                            Log.d("LibraryViewModel", "Fetched items: $updatedDataList")
                            _items.postValue(updatedDataList)
                        }
                    }
                }
            }
        }
    }

    fun fetchItemsByUserId(userId: String) {
        repository.fetchDataByUserId(userId) { dataList ->
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
}