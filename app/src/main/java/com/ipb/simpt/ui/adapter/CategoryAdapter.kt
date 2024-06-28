package com.ipb.simpt.ui.adapter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.ipb.simpt.databinding.ItemCategoryListBinding
import com.ipb.simpt.model.CategoryModel
import com.ipb.simpt.ui.adapter.filter.CategoryFilter
import com.ipb.simpt.ui.dosen.library.DosenCategoryListActivity.Companion.REQUEST_CODE
import com.ipb.simpt.ui.dosen.library.detail.DosenCategoryDetailActivity

class CategoryAdapter(
    private val context: Context,
    var categoryList: ArrayList<CategoryModel>,
    private val categoryName: String,
    private val categoryValue: String
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>(), Filterable {

    private var filterList: ArrayList<CategoryModel> = categoryList
    private lateinit var filter: Filter

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = ItemCategoryListBinding.inflate(LayoutInflater.from(context), parent, false)
        return CategoryViewHolder(binding)
    }

    override fun getItemCount(): Int = categoryList.size // number of items in list

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        // get data
        val model = categoryList[position]

        // set data
        holder.binding.tvCategory.text = model.name

        // handle click, start data list activity, also pass data id, title
        holder.itemView.setOnClickListener {
            val intent = Intent(context, DosenCategoryDetailActivity::class.java)
            intent.putExtra("ITEM_ID", model.id)
            intent.putExtra("ITEM_NAME", model.name)
            intent.putExtra("CATEGORY_NAME", categoryName)
            intent.putExtra("CATEGORY_VALUE", categoryValue)
            (context as Activity).startActivityForResult(intent, REQUEST_CODE)
        }
    }

    // viewHolder class to hold/init UI views for item_approval_list.xml
    inner class CategoryViewHolder(val binding: ItemCategoryListBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun getFilter(): Filter {
        if (!::filter.isInitialized) {
            filter = CategoryFilter(filterList, this)
        }
        return filter
    }

    fun updateData(newData: ArrayList<CategoryModel>) {
        categoryList = newData
        filterList = ArrayList(newData)
        notifyDataSetChanged()
    }

}