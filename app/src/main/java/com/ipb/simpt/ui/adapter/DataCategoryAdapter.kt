package com.ipb.simpt.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ipb.simpt.databinding.ItemCategoryDetailListBinding
import com.ipb.simpt.model.DataModel
import com.ipb.simpt.ui.dosen.library.DosenCategoryViewModel

class DataCategoryAdapter(
    private val context: Context,
    private var dataList: List<DataModel>,
    private val viewModel: DosenCategoryViewModel,
    private val categoryName: String,
    private val categoryValue: String?
) : RecyclerView.Adapter<DataCategoryAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: ItemCategoryDetailListBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: DataModel) {
            when (categoryName) {
                "Komoditas" -> {
                    viewModel.fetchCategoryName("Komoditas", data.komoditasId) { name ->
                        binding.tvTitle.text = name
                    }
                }

                "Penyakit" -> {
                    viewModel.fetchCategoryName("Penyakit", data.penyakitId) { name ->
                        binding.tvTitle.text = name
                    }
                }

                "Gejala Penyakit" -> {
                    viewModel.fetchCategoryName("Gejala Penyakit", data.gejalaId) { name ->
                        binding.tvTitle.text = name
                    }
                }

                "Kategori Pathogen" -> {
                    viewModel.fetchPathogenName(categoryValue, data.pathogenId) { name ->
                        binding.tvTitle.text = name
                    }
                }

                else -> {
                    binding.tvTitle.text = "Unknown Category"
                }
            }

            viewModel.fetchUserName(data.uid) { userName ->
                binding.tvUser.text = userName
            }

            binding.tvDescription.text = data.deskripsi

            Glide.with(context)
                .load(data.url)
                .into(binding.ivImage)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCategoryDetailListBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(dataList[position])
    }

    override fun getItemCount(): Int = dataList.size

    fun updateData(newData: ArrayList<DataModel>) {
        dataList = newData
        notifyDataSetChanged()
    }
}