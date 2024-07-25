package com.ipb.simpt.ui.filter

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ipb.simpt.model.FilterModel
import com.ipb.simpt.ui.dosen.approval.ApprovalViewModel
import com.ipb.simpt.ui.mahasiswa.library.LibraryViewModel

class FilterViewModel : ViewModel() {

    private val _filters = MutableLiveData<FilterModel>()
    val filters: LiveData<FilterModel> = _filters

    fun setFilters(filterModel: FilterModel) {
        _filters.value = filterModel
    }

    fun applyFilters(viewModel: BaseFilterableViewModel) {
        filters.value?.let {
            viewModel.applyFilters(it)
        }
    }
}
