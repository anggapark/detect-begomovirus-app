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
import com.ipb.simpt.ui.filter.FilterFragment
import com.ipb.simpt.ui.filter.FilterViewModel

class LibraryFragment : Fragment() {

    private var _binding: FragmentLibraryBinding? = null
    private val binding get() = _binding!!

    private lateinit var dataList: ArrayList<DataModel>
    private lateinit var viewModel: LibraryViewModel
    private lateinit var filterViewModel: FilterViewModel
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

        filterViewModel = ViewModelProvider(requireActivity()).get(FilterViewModel::class.java)
        viewModel = ViewModelProvider(this).get(LibraryViewModel::class.java)

//        setupLayoutSwitcher()
        setupRecyclerView()
        setupSearchFilter()
        setupObservers()
        setupPagination()

        viewModel.fetchApprovedItemsByPage(viewModel.currentPage)
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

//        binding.rvLibrary.addOnScrollListener(object : RecyclerView.OnScrollListener() {
//            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
//                super.onScrolled(recyclerView, dx, dy)
//
//                val layoutManager = recyclerView.layoutManager ?: return
//
//                val visibleItemCount = layoutManager.childCount
//                val totalItemCount = layoutManager.itemCount
//                val firstVisibleItemPosition = when (layoutManager) {
//                    is LinearLayoutManager -> layoutManager.findFirstVisibleItemPosition()
//                    is GridLayoutManager -> layoutManager.findFirstVisibleItemPosition()
//                    else -> return
//                }
//
//                if (!isLoadingMore && !viewModel.isLoading.value!! &&
//                    (visibleItemCount + firstVisibleItemPosition >= totalItemCount) &&
//                    firstVisibleItemPosition >= 0 && totalItemCount >= viewModel.pageSize) {
//
//                    isLoadingMore = true
//                    viewModel.fetchMoreApprovedItems()
//                }
//            }
//        })
//
//
//        // Observe loading more state
//        viewModel.isLoadingMore.observe(viewLifecycleOwner, Observer { isLoadingMore ->
//            this.isLoadingMore = isLoadingMore
//            showLoading(isLoadingMore)
//        })
    }


    private fun setupPagination() {
        binding.btnPrevious.setOnClickListener {
            if (viewModel.currentPage > 1 && !viewModel.isLoading.value!!) {
                viewModel.goToPage(viewModel.currentPage - 1)
                updateButtonStates()
                scrollToFirstItem() // Scroll to first item on page change
            }
        }

        binding.btnNext.setOnClickListener {
            if (viewModel.canLoadMore && !viewModel.isLoading.value!!) {
                viewModel.goToPage(viewModel.currentPage + 1)
                updateButtonStates()
                scrollToFirstItem() // Scroll to first item on page change
            }
        }

        binding.ivFilter.setOnClickListener {
            showFilterFragment()
        }
    }

    private fun showFilterFragment() {
        val filterFragment = FilterFragment()

        // Create a bundle with the current filter data
        val args = Bundle()
        val currentFilters = filterViewModel.filters.value
        args.putParcelable("currentFilters", currentFilters)
        filterFragment.arguments = args

        filterFragment.show(parentFragmentManager, "FilterFragment")
    }

    private fun updateButtonStates() {
        binding.btnPrevious.isEnabled = viewModel.currentPage > 1
        binding.btnNext.isEnabled = viewModel.canLoadMore
        binding.tvPageNumber.text = "${viewModel.currentPage}"

    }

    private fun scrollToFirstItem() {
        (binding.rvLibrary.layoutManager as? LinearLayoutManager)?.scrollToPosition(0)
    }

    private fun setupObservers() {
        // Observe filter
//        filterViewModel.filters.observe(viewLifecycleOwner, Observer { filters ->
//            filters?.let {
//                dataList.clear()
//                viewModel.resetPagination()
//                viewModel.applyFilters(it)
//            }
//        })

        // Observe data changes
        viewModel.items.observe(viewLifecycleOwner, Observer { items ->
            if (items != null) {
                dataList.clear()
                dataList.addAll(items)
                adapter.notifyDataSetChanged()
                showLoading(false)
                updateButtonStates()
            } else {
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