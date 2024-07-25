package com.ipb.simpt.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ipb.simpt.R
import com.ipb.simpt.databinding.ItemApprovalGridBinding
import com.ipb.simpt.databinding.ItemMydataListBinding
import com.ipb.simpt.model.DataModel
import com.ipb.simpt.ui.filter.MyDataFilter
import com.ipb.simpt.ui.mahasiswa.library.LibraryViewModel

class MyDataAdapter(
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
        return if (isGridLayout) R.layout.item_approval_grid else R.layout.item_mydata_list
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == R.layout.item_approval_grid) {
            val binding = ItemApprovalGridBinding.inflate(inflater, parent, false)
            GridViewHolder(binding)
        } else {
            val binding = ItemMydataListBinding.inflate(inflater, parent, false)
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

    inner class ListViewHolder(private val binding: ItemMydataListBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: DataModel) {
            binding.tvKomoditas.text = data.komoditasName
            binding.tvPenyakit.text = data.penyakitName
            binding.tvPathogen.text = data.pathogenName
            binding.tvGejala.text = data.gejalaName
            binding.tvDeskripsi.text = data.deskripsi
            binding.tvStatus.text = data.status
            when (data.status) {
                "Pending" -> binding.tvStatus.background =
                    AppCompatResources.getDrawable(itemView.context, R.drawable.bg_pending)

                "Approved" -> binding.tvStatus.background =
                    AppCompatResources.getDrawable(itemView.context, R.drawable.bg_approved)

                "Rejected" -> binding.tvStatus.background =
                    AppCompatResources.getDrawable(itemView.context, R.drawable.bg_rejected)
            }
            Glide.with(context)
                .load(data.url)
                .into(binding.ivImage)

            binding.root.setOnClickListener { itemClick(data) }
        }
    }

    inner class GridViewHolder(private val binding: ItemApprovalGridBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: DataModel) {
            binding.tvTitle.text = data.komoditasName

            viewModel.fetchUserName(data.uid) { userName ->
                binding.tvUser.text = userName
            }
            binding.tvDescription.text = data.deskripsi
            binding.tvStatus.text = data.status
            when (data.status) {
                "Pending" -> binding.tvStatus.background =
                    AppCompatResources.getDrawable(itemView.context, R.drawable.bg_pending)

                "Approved" -> binding.tvStatus.background =
                    AppCompatResources.getDrawable(itemView.context, R.drawable.bg_approved)

                "Rejected" -> binding.tvStatus.background =
                    AppCompatResources.getDrawable(itemView.context, R.drawable.bg_rejected)

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
            filter = MyDataFilter(filterList, this)
        }
        return filter
    }
}