package com.ipb.simpt.ui.filter

import android.widget.Filter
import com.ipb.simpt.model.UserModel
import com.ipb.simpt.ui.adapter.AdminAdapter

class AdminFilter(
    private var userList: ArrayList<UserModel>,
    private var adapter: AdminAdapter
) : Filter() {

    override fun performFiltering(constraint: CharSequence?): FilterResults {
        // Convert input to uppercase for case-insensitive search
        val charString = constraint?.toString() ?: ""

        // If the input is not empty, filter the category list.
        // Otherwise, return the user list.
        if (charString.isEmpty()) {
            adapter.filterList = userList
        } else {
            // Use the 'filter' function to create a new list
            // that only includes categories that contain the input
            val filteredList = userList.filter { user ->
                user.userName.contains(charString, true) ||
                        user.userNim.contains(charString, true) ||
                        user.email.contains(charString, true)
                        //user.userType.contains(charString, true)
            }.toMutableList()
            adapter.filterList = filteredList as ArrayList<UserModel>
        }

        // Return the filtered list wrapped in a FilterResults object
        return FilterResults().apply { values = adapter.filterList }
    }

    override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
        adapter.userList = results?.values as ArrayList<UserModel>
        adapter.notifyDataSetChanged()
    }
}
