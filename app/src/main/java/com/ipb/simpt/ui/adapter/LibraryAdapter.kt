package com.ipb.simpt.ui.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ipb.simpt.R
import com.ipb.simpt.databinding.ItemLibraryGridBinding
import com.ipb.simpt.databinding.ItemLibraryListBinding
import com.ipb.simpt.model.DataModel
import com.ipb.simpt.ui.adapter.filter.LibraryFilter
import com.ipb.simpt.ui.mahasiswa.library.LibraryViewModel
import com.ipb.simpt.utils.FileHelper

class LibraryAdapter(
    private val context: Context,
    var dataList: ArrayList<DataModel>,
    private val viewModel: LibraryViewModel,
    private val itemClick: (DataModel) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), Filterable {

    var filterList: ArrayList<DataModel> = dataList
    private lateinit var filter: Filter
    private var isGridLayout = false

    fun setLayoutType(isGridLayout: Boolean) {
        this.isGridLayout = isGridLayout
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return if (isGridLayout) R.layout.item_library_grid else R.layout.item_library_list
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == R.layout.item_library_grid) {
            val binding = ItemLibraryGridBinding.inflate(inflater, parent, false)
            GridViewHolder(binding)
        } else {
            val binding = ItemLibraryListBinding.inflate(inflater, parent, false)
            ListViewHolder(binding)
        }
    }
    override fun getItemCount() = dataList.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val data = dataList[position]
        if (holder is ListViewHolder) {
            holder.bind(data)
        } else if (holder is GridViewHolder) {
            holder.bind(data)
        }
    }

    inner class ListViewHolder(private val binding: ItemLibraryListBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: DataModel) {
            binding.tvKomoditas.text = data.komoditasName
            binding.tvPenyakit.text = data.penyakitName
            binding.tvPathogen.text = data.pathogenName
            binding.tvGejala.text = data.gejalaName
            binding.tvUser.text = data.userName
            binding.tvDate.text = FileHelper.formatTimeStamp(data.timestamp)
//            binding.tvDeskripsi.text = data.deskripsi

            Glide.with(context)
                .load(data.url)
                .into(binding.ivImage)

            binding.root.setOnClickListener { itemClick(data) }
        }
    }

    inner class GridViewHolder(private val binding: ItemLibraryGridBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: DataModel) {
            binding.tvTitle.text = data.komoditasName

            viewModel.fetchUserName(data.uid) { userName ->
                binding.tvUser.text = userName
            }

            Glide.with(context)
                .load(data.url)
                .into(binding.ivImage)

            binding.root.setOnClickListener { itemClick(data) }
        }
    }

    fun updateData(newData: ArrayList<DataModel>) {
        dataList = newData
        filterList = ArrayList(newData)
        notifyDataSetChanged()
    }

    override fun getFilter(): Filter {
        if (!::filter.isInitialized) {
            filter = LibraryFilter(filterList, this)
        }
        return filter
    }
}