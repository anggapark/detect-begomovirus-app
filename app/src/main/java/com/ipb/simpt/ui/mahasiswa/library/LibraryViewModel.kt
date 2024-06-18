package com.ipb.simpt.ui.mahasiswa.library

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ipb.simpt.model.DataModel
import com.ipb.simpt.repository.DataRepository

class LibraryViewModel : ViewModel() {

    private val repository = DataRepository()

    private val _items = MutableLiveData<List<DataModel>>()
    val items: LiveData<List<DataModel>> get() = _items

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
                            fetchAndCacheUserDetails(dataModel) {
                                callback()
                            }
                        }
                    }
                }
            }
        }
    }

    // Method to fetch and cache user details
    private fun fetchAndCacheUserDetails(dataModel: DataModel, callback: () -> Unit) {
        repository.fetchUserName(dataModel.uid) { userName ->
            dataModel.userName = userName
            repository.fetchUserNim(dataModel.uid) { userNim ->
                dataModel.userNim = userNim
                callback()
            }
        }
    }
}