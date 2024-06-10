package com.ipb.simpt.ui.dosen.library.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ipb.simpt.R
import com.ipb.simpt.model.DataModel
import com.ipb.simpt.ui.adapter.DataCategoryAdapter

class DosenCategoryRecyclerViewFragment : Fragment() {

    private val viewModel: DosenCategoryRecyclerViewViewModel by activityViewModels()
    private lateinit var progressBar: ProgressBar
    private lateinit var itemId: String
    private lateinit var itemName: String
    private lateinit var categoryName: String
    private var categoryValue: String? = null
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: DataCategoryAdapter
    private lateinit var dataArrayList: ArrayList<DataModel>

    companion object {
        private const val ARG_ITEM_ID = "ITEM_ID"
        private const val ARG_ITEM_NAME = "ITEM_NAME"
        private const val ARG_CATEGORY_NAME = "CATEGORY_NAME"
        private const val ARG_CATEGORY_VALUE = "CATEGORY_VALUE"

        fun newInstance(
            itemId: String,
            itemName: String,
            categoryName: String,
            categoryValue: String?
        ) = DosenCategoryRecyclerViewFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_ITEM_ID, itemId)
                putString(ARG_ITEM_NAME, itemName)
                putString(ARG_CATEGORY_NAME, categoryName)
                putString(ARG_CATEGORY_VALUE, categoryValue)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            itemId = it.getString(ARG_ITEM_ID) ?: ""
            itemName = it.getString(ARG_ITEM_NAME) ?: ""
            categoryName = it.getString(ARG_CATEGORY_NAME) ?: ""
            categoryValue = it.getString(ARG_CATEGORY_VALUE)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_dosen_category_recycler_view, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.rv_data)
        progressBar = view.findViewById(R.id.progressBar)

        setupRecyclerView()

        viewModel.categoryData.observe(viewLifecycleOwner, Observer { categoryData ->
            if (categoryData != null) {
                dataArrayList.clear()
                dataArrayList.addAll(categoryData)
                adapter.updateData(dataArrayList)
                showLoading(false)
            }
        })

        // Initial fetch
        fetchData()
    }

    fun fetchData() {
        showLoading(true)
        viewModel.fetchDataByCategory(categoryName, itemId, categoryValue)
    }

    private fun showLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun setupRecyclerView() {
        dataArrayList = ArrayList()
        adapter = DataCategoryAdapter(
            requireContext(),
            dataArrayList,
            viewModel,
            categoryName,
            categoryValue
        )
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
    }
}