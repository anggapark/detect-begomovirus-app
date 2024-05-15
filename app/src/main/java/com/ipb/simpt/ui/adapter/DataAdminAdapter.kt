package com.ipb.simpt.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.ipb.simpt.data.model.CategoryModel
import com.ipb.simpt.data.model.DataModel
import com.ipb.simpt.databinding.ItemListBinding
import com.ipb.simpt.utils.Utils

class DataAdminAdapter(
    private val context: Context,
    var dataArrayList: ArrayList<DataModel>
) : RecyclerView.Adapter<DataAdminAdapter.HolderDataAdmin>(), Filterable {

    private var filterList: ArrayList<DataModel> = dataArrayList
    private var filter: DataAdminFilter? = null
    private lateinit var binding: ItemListBinding

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderDataAdmin {
        // Inflate / bind category_list.xml
        binding = ItemListBinding.inflate(LayoutInflater.from(context), parent, false)
        return HolderDataAdmin(binding.root)
    }

    override fun getItemCount(): Int = dataArrayList.size // number of items in list

    override fun onBindViewHolder(holder: HolderDataAdmin, position: Int) {
        // get data
        val model = dataArrayList[position]
        val dataId = model.id
        val categoryId = model.categoryId
        val title = model.title
        val description = model.description
        val dataUrl = model.url
        val timestamp = model.timestamp

        // convert timestamp to dd/MM/yyyy format
        val formattedData = Utils.formatTimeStamp(timestamp)

        // set data
        holder.tvTitle.text = title
        holder.tvDescription.text = description

        // load further details
        // load category
        Utils.loadCategory(categoryId, holder.tvUser)

        //TODO: User details

        // load image thumbnail
        Utils.loadDataFromUrl(dataUrl, holder.imageView)
    }

    override fun getFilter(): Filter {
        if (filter == null){
            filter = DataAdminFilter(filterList, this)
        }
        return filter as DataAdminFilter
    }

    // view holder class for item_list.xml
    inner class HolderDataAdmin(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // UI views of item_list.xml
        val imageView = binding.imageView
        val tvTitle = binding.tvTitle
        val tvDescription = binding.tvDescription
        val tvUser = binding.tvUser
        // val ivMore = binding.ivMore
    }

}