package com.ipb.simpt.ui.dosen.approval

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.ipb.simpt.model.DataModel
import com.ipb.simpt.repository.DataRepository

class ApprovalDetailViewModel : ViewModel() {
    private val repository = DataRepository()

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

    fun approveData(itemId: String) {
        repository.updateApprovalStatus(itemId, "Approved")
    }

    fun rejectData(itemId: String) {
        repository.updateApprovalStatus(itemId, "Rejected")
    }
}
