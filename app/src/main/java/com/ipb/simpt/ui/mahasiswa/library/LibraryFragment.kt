package com.ipb.simpt.ui.mahasiswa.library

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ipb.simpt.R
import com.ipb.simpt.databinding.FragmentLibraryBinding
import com.ipb.simpt.model.DataModel
import com.ipb.simpt.ui.adapter.LibraryAdapter

class LibraryFragment : Fragment() {

    private var _binding: FragmentLibraryBinding? = null
    private val binding get() = _binding!!

    private lateinit var dataList: ArrayList<DataModel>
    private lateinit var viewModel: LibraryViewModel
    private lateinit var adapter: LibraryAdapter
    private var isGridLayout: Boolean = false
    private var isLoadingMore = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLibraryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this).get(LibraryViewModel::class.java)

//        setupLayoutSwitcher()
        setupRecyclerView()
        setupSearchFilter()
        setupObservers()

        viewModel.fetchInitialApprovedItems()
    }

    private fun setupRecyclerView() {
        showLoading(true)
        dataList = ArrayList()

        adapter = LibraryAdapter(requireContext(), dataList, viewModel) { dataModel ->
            val intent = Intent(requireContext(), LibraryDetailActivity::class.java).apply {
                putExtra("ITEM_ID", dataModel.id)
            }
            startActivity(intent)
        }

        binding.rvLibrary.adapter = adapter
        binding.rvLibrary.layoutManager =
            LinearLayoutManager(requireContext()) // Default to list layout

        binding.rvLibrary.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val layoutManager = recyclerView.layoutManager ?: return

                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = when (layoutManager) {
                    is LinearLayoutManager -> layoutManager.findFirstVisibleItemPosition()
                    is GridLayoutManager -> layoutManager.findFirstVisibleItemPosition()
                    else -> return
                }

                if (!isLoadingMore && !viewModel.isLoading.value!! &&
                    (visibleItemCount + firstVisibleItemPosition >= totalItemCount) &&
                    firstVisibleItemPosition >= 0 && totalItemCount >= viewModel.pageSize) {

                    isLoadingMore = true
                    viewModel.fetchMoreApprovedItems()
                }
            }
        })


        // Observe loading more state
        viewModel.isLoadingMore.observe(viewLifecycleOwner, Observer { isLoadingMore ->
            this.isLoadingMore = isLoadingMore
            showLoading(isLoadingMore)
        })
    }

    private fun setupObservers() {
        // Observe data changes
        viewModel.items.observe(viewLifecycleOwner, Observer { items ->
            if (items != null) {
                Log.d("LibraryFragment", "Items found: $items")
                dataList.clear()
                dataList.addAll(items)
                adapter.updateData(dataList)
                showLoading(false)
            } else {
                Log.d("LibraryFragment", "No items found")
                showLoading(false)
            }
        })

        // Observe loading state
        viewModel.isLoading.observe(viewLifecycleOwner, Observer { isLoading ->
            showLoading(isLoading)
        })

        // Observe error state
        viewModel.error.observe(viewLifecycleOwner, Observer { error ->
            error?.let {
                Log.e("LibraryFragment", it)
                Toast.makeText(requireContext(), "Error: $it", Toast.LENGTH_SHORT).show()
                viewModel.errorHandled()  // Reset the error state after handling it
            }
        })
    }

//    private fun setupLayoutSwitcher() {
//        binding.btnList.setOnClickListener {
//            isGridLayout = false
//            applyLayoutManager()
//            adapter.setLayoutType(isGridLayout)
//            updateLayoutSwitcherBackground()
//        }
//
//        binding.btnGrid.setOnClickListener {
//            isGridLayout = true
//            applyLayoutManager()
//            adapter.setLayoutType(isGridLayout)
//            updateLayoutSwitcherBackground()
//        }
//    }

//    private fun updateLayoutSwitcherBackground() {
//        if (isGridLayout) {
//            binding.btnList.setBackgroundResource(R.drawable.bg_text)
//            binding.btnGrid.setBackgroundResource(R.drawable.bg_text_selected)
//        } else {
//            binding.btnList.setBackgroundResource(R.drawable.bg_text_selected)
//            binding.btnGrid.setBackgroundResource(R.drawable.bg_text)
//        }
//    }

    private fun applyLayoutManager() {
        binding.rvLibrary.layoutManager = if (isGridLayout) {
            GridLayoutManager(requireContext(), 2)
        } else {
            LinearLayoutManager(requireContext())
        }
    }

    private fun setupSearchFilter() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
                // called as and when user type anything
                try {
                    adapter.filter.filter(s)
                } catch (e: Exception) {

                }
            }

            override fun afterTextChanged(p0: Editable?) {

            }
        })
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}