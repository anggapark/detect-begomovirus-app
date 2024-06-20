package com.ipb.simpt.ui.adapter

import android.widget.Filter
import com.ipb.simpt.model.DataModel

class ApprovalFilter(
    private var dataList: ArrayList<DataModel>,
    private var adapter: ApprovalAdapter
) : Filter() {

    override fun performFiltering(constraint: CharSequence?): FilterResults {
        // Convert input to uppercase for case-insensitive search
        val charString = constraint?.toString() ?: ""

        // If the input is not empty, filter the category list.
        // Otherwise, return the data list.
        if (charString.isEmpty()) {
            adapter.filterList = dataList
        } else {
            // Use the 'filter' function to create a new list
            // that only includes categories that contain the input
            val filteredList = dataList.filter { data ->
                data.komoditasName.contains(charString, true) ||
                        data.penyakitName.contains(charString, true) ?: false ||
                        data.gejalaName.contains(charString, true) ?: false ||
                        data.pathogenName.contains(charString, true) ?: false ||
                        data.kategoriPathogen.contains(charString, true) ||
                        data.dataset.contains(charString, true) ||
                        data.deskripsi.contains(charString, true) ||
                        data.status.contains(charString, true) ||
                        data.userName.contains(charString, true) ||
                        data.userNim.contains(charString, true)
            }.toMutableList()
            adapter.filterList = filteredList as ArrayList<DataModel>
        }

        // Return the filtered list wrapped in a FilterResults object
        return FilterResults().apply { values = adapter.filterList }
    }

    override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
        adapter.dataList = results?.values as ArrayList<DataModel>
        adapter.notifyDataSetChanged()
    }
}
