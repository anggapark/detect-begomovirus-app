package com.ipb.simpt.ui.adapter

import android.provider.ContactsContract.Data
import android.widget.Filter
import com.ipb.simpt.data.model.DataModel

// used to filter data from recyclerview | search data from data list in recyclerview
class DataAdminFilter (
    // adapter in which filter need to be implemented
    val filterList: ArrayList<DataModel>,
    // adapter in which filter need to be implemented
    var dataAdminAdapter: DataAdminAdapter
) : Filter() {
    override fun performFiltering(constraint: CharSequence?): FilterResults {
        var constraint: CharSequence? = constraint // value to search
        val result = FilterResults()
        // values should not be null and not empty
        if (constraint != null && constraint.isNotEmpty()){
            // change to upper case or lower case to avoid case sensitivity
            constraint = constraint.toString().lowercase()
            var filteredModels = ArrayList<DataModel>()
            for (i in filterList.indices){
                // validate if match
                if (filterList[i].categoryId.lowercase().contains(constraint)){
                    // searched value is similar to value in list, add to filtered list
                    filteredModels.add(filterList[i])
                }
            }
            result.count = filteredModels.size
            result.values = filteredModels
        }
        else{
            // value is either null or empty, return all data
            result.count = filterList.size
            result.values = filterList
        }
        return result
    }

    override fun publishResults(constraint: CharSequence, result: FilterResults) {
        // apply filter changes
        dataAdminAdapter.dataArrayList = result.values as ArrayList<DataModel>

        // notify changes
        dataAdminAdapter.notifyDataSetChanged()
    }


}