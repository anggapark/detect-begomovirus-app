package com.ipb.simpt.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.appcompat.content.res.AppCompatResources
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ipb.simpt.R
import com.ipb.simpt.databinding.ItemApprovalGridBinding
import com.ipb.simpt.databinding.ItemApprovalListBinding
import com.ipb.simpt.model.DataModel
import com.ipb.simpt.repository.DataRepository
import com.ipb.simpt.ui.dosen.approval.ApprovalViewModel

class ApprovalAdapter(
    private val context: Context,
    var dataList: ArrayList<DataModel>,
    private val viewModel: ApprovalViewModel,
    private val itemClick: (DataModel) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), Filterable {

    var filterList: ArrayList<DataModel> = ArrayList(dataList)
    private var filter: DataFilter? = null
    private var isGridLayout = false

    init {
        filter = DataFilter(dataList, this)
    }

    fun setLayoutType(isGridLayout: Boolean) {
        this.isGridLayout = isGridLayout
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return if (isGridLayout) R.layout.item_approval_grid else R.layout.item_approval_list
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == R.layout.item_approval_grid) {
            val binding =
                ItemApprovalGridBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            GridViewHolder(binding)
        } else {
            val binding =
                ItemApprovalListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
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

    inner class ListViewHolder(private val binding: ItemApprovalListBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: DataModel) {
            viewModel.fetchCategoryName("Komoditas", data.komoditasId) { name ->
                binding.tvTitle.text = name
            }

            viewModel.fetchUserName(data.uid) { userName ->
                binding.tvUser.text = userName
            }

            binding.tvDescription.text = data.deskripsi
            binding.tvStatus.text = data.status
            when (data.status) {
                "Pending" -> binding.tvStatus.background = AppCompatResources.getDrawable(itemView.context, R.drawable.bg_pending)
                "Approved" -> binding.tvStatus.background = AppCompatResources.getDrawable(itemView.context, R.drawable.bg_approved)
                "Rejected" -> binding.tvStatus.background = AppCompatResources.getDrawable(itemView.context, R.drawable.bg_rejected)
            }

            // set to imageView
            Glide.with(context)
                .load(data.url)
                .into(binding.ivImage)

            binding.root.setOnClickListener { itemClick(data) }
        }
    }

    inner class GridViewHolder(private val binding: ItemApprovalGridBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: DataModel) {
            viewModel.fetchCategoryName("Komoditas", data.komoditasId) { name ->
                binding.tvTitle.text = name
            }

            viewModel.fetchUserName(data.uid) { userName ->
                binding.tvUser.text = userName
            }

            binding.tvDescription.text = data.deskripsi
            binding.tvStatus.text = data.status
            when (data.status) {
                "Pending" -> binding.tvStatus.background = AppCompatResources.getDrawable(itemView.context, R.drawable.bg_pending)
                "Approved" -> binding.tvStatus.background = AppCompatResources.getDrawable(itemView.context, R.drawable.bg_approved)
                "Rejected" -> binding.tvStatus.background = AppCompatResources.getDrawable(itemView.context, R.drawable.bg_rejected)

            }

            // set to imageView
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
        return filter as Filter
    }
}
