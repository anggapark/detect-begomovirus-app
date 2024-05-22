package com.ipb.simpt.utils

import com.ipb.simpt.model.DataModel

class SearchHandler(private val submissions: List<DataModel>, private val updateResults: (List<DataModel>) -> Unit) {

    fun search(query: String) {
        val filtered = submissions.filter {
            it.deskripsi.contains(query, ignoreCase = true)
        }
        updateResults(filtered)
    }
}
