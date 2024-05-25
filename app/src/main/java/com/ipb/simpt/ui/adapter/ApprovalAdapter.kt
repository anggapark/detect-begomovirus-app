package com.ipb.simpt.ui.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ipb.simpt.databinding.ItemApprovalListBinding
import com.ipb.simpt.model.DataModel
import com.ipb.simpt.ui.dosen.approval.ApprovalViewModel

class ApprovalAdapter(
    private val context: Context,
    private var dataList: ArrayList<DataModel>,
    private val viewModel: ApprovalViewModel,
    private val itemClick: (DataModel) -> Unit
) : RecyclerView.Adapter<ApprovalAdapter.ViewHolder>() {

    private lateinit var binding: ItemApprovalListBinding

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        binding = ItemApprovalListBinding.inflate(LayoutInflater.from(context), parent, false)
        return ViewHolder(binding.root)
    }

    override fun getItemCount() = dataList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(dataList[position])
    }

    // viewHolder class to hold/init UI views for item_approval_list.xml
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private var title: TextView = binding.tvTitle
        private var description: TextView = binding.tvDescription
        private var user: TextView = binding.tvUser
        private var status: TextView = binding.tvStatus

        fun bind(data: DataModel) {
            viewModel.fetchCategoryName("Komoditas", data.komoditasId) { name ->
                title.text = name
            }

            viewModel.fetchUserName(data.uid) { userName ->
                user.text = userName
            }

            description.text = data.deskripsi
            status.text = data.status

            itemView.setOnClickListener {
                itemClick(data)
            }
        }
    }

    fun updateData(newData: ArrayList<DataModel>) {
        dataList = newData
        notifyDataSetChanged()
        Log.d("ApprovalAdapter", "Data updated with ${newData.size} items")
    }
}

//class ApprovalAdapter(private val onClick: (DataModel) -> Unit) :
//    ListAdapter<DataModel, ApprovalAdapter.ApprovalViewHolder>(DataModelDiffCallback()) {
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ApprovalViewHolder {
//        val binding = ItemApprovalListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
//        return ApprovalViewHolder(binding)
//    }
//
//    override fun onBindViewHolder(holder: ApprovalViewHolder, position: Int) {
//        holder.bind(getItem(position), onClick)
//    }
//
//    class ApprovalViewHolder(private val binding: ItemApprovalListBinding) : RecyclerView.ViewHolder(binding.root) {
//        fun bind(dataModel: DataModel, onClick: (DataModel) -> Unit) {
//            // TODO: FIX tvTitle to change the name to whatever current tabs's category
//            binding.tvTitle.text = dataModel.komoditasId
//            binding.tvDescription.text = dataModel.deskripsi
//            binding.tvUser.text = dataModel.uid
//            binding.tvStatus.text = dataModel.status
//            binding.root.setOnClickListener { onClick(dataModel) }
//        }
//    }
//
//    class DataModelDiffCallback : DiffUtil.ItemCallback<DataModel>() {
//        override fun areItemsTheSame(oldItem: DataModel, newItem: DataModel): Boolean = oldItem.id == newItem.id
//        override fun areContentsTheSame(oldItem: DataModel, newItem: DataModel): Boolean = oldItem == newItem
//    }
//}
