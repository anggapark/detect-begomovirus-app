package com.ipb.simpt.ui.mahasiswa.add

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class AddDataViewModel : ViewModel() {
    private val _activityImgUri = MutableLiveData<Uri?>()
    val activityImgUri: LiveData<Uri?> get() = _activityImgUri

    private val _bottomSheetImgUri = MutableLiveData<Uri?>()
    val bottomSheetImgUri: LiveData<Uri?> get() = _bottomSheetImgUri

    fun setActivityImgUri(uri: Uri?) {
        _activityImgUri.value = uri
    }

    fun setBottomSheetImgUri(uri: Uri?) {
        _bottomSheetImgUri.value = uri
    }

    fun resetImgUris() {
        _activityImgUri.value = null
        _bottomSheetImgUri.value = null
    }

}