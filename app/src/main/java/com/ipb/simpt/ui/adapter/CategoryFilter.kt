package com.ipb.simpt.ui.adapter

import android.widget.Filter
import com.ipb.simpt.data.model.CategoryModel

class CategoryFilter(
    private var filterList: ArrayList<CategoryModel>,
    private var adapter: CategoryAdapter
) : Filter() {


    override fun performFiltering(constraint: CharSequence?): FilterResults {
        // Convert input to uppercase for case-insensitive search
        val constraintString = constraint.toString().uppercase()

        // If the input is not empty, filter the category list.
        // Otherwise, return the original list.
        val filteredModel = if (constraintString.isEmpty()) {
            filterList
        } else {
            // Use the 'filter' function to create a new list
            // that only includes categories that contain the input
            filterList.filter {
                it.category.uppercase().contains(constraintString)
            } as ArrayList<CategoryModel>
        }

        // Return the filtered list wrapped in a FilterResults object
        return FilterResults().apply {
            count = filteredModel.size // The number of items in the filtered list
            values = filteredModel // The filtered list itself
        }
    }

    override fun publishResults(constraint: CharSequence?, results: FilterResults) {
        // Update the adapter's data with the filtered list
        adapter.categoryArrayList = results.values as ArrayList<CategoryModel>

        // Notify the adapter that the data has changed so it can refresh the UI
        adapter.notifyDataSetChanged()
    }
}