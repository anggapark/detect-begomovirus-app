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
import com.ipb.simpt.databinding.ItemApprovalListBinding
import com.ipb.simpt.model.UserModel
import com.ipb.simpt.ui.filter.AdminFilter

class AdminAdapter(
    private val context: Context,
    var userList: ArrayList<UserModel>,
    private val itemClick: (UserModel) -> Unit
) : RecyclerView.Adapter<AdminAdapter.UserViewHolder>(), Filterable {

    var filterList: ArrayList<UserModel> = ArrayList(userList)
    private var filter: AdminFilter? = null

    init {
        filter = AdminFilter(userList, this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ItemApprovalListBinding.inflate(LayoutInflater.from(context), parent, false)
        return UserViewHolder(binding)
    }

    override fun getItemCount() = userList.size

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = userList[position]

        holder.binding.apply {
            tvTitle.text = user.userName
            tvUser.text = user.userNim
            tvDescription.text = user.email
            tvStatus.text = user.userType
            tvStatus.background = when (user.userType) {
                "user" -> AppCompatResources.getDrawable(context, R.drawable.bg_pending)
                "dosen" -> AppCompatResources.getDrawable(context, R.drawable.bg_approved)
                else -> tvStatus.background
            }

            Glide.with(context)
                .load(user.profileImage)
                .placeholder(R.drawable.ic_profile)
                .into(ivImage)

            root.setOnClickListener { itemClick(user) }
        }

    }

    inner class UserViewHolder(val binding: ItemApprovalListBinding) :
        RecyclerView.ViewHolder(binding.root)

    fun updateData(newData: ArrayList<UserModel>) {
        userList = newData
        filterList = ArrayList(newData)
        notifyDataSetChanged()
    }

    override fun getFilter(): Filter {
        return filter as Filter
    }
}
