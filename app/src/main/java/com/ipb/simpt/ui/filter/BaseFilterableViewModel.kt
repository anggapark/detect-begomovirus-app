package com.ipb.simpt.ui.filter

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ipb.simpt.model.FilterModel

abstract class BaseFilterableViewModel : ViewModel() {
    val filters = MutableLiveData<FilterModel>()

    fun setFilters(filterModel: FilterModel) {
        filters.value = filterModel
        applyFilters(filterModel)
    }

    abstract fun applyFilters(filterModel: FilterModel)
}
