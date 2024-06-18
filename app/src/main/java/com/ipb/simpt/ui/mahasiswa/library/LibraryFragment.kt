package com.ipb.simpt.ui.mahasiswa.library

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.ipb.simpt.databinding.FragmentLibraryBinding
import com.ipb.simpt.ui.adapter.LibraryAdapter

class LibraryFragment : Fragment() {

    private var _binding: FragmentLibraryBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: LibraryViewModel
    private lateinit var adapter: LibraryAdapter
    private var isGridLayout : Boolean = false

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

        setupRecyclerView()
        setupLayoutSwitcher()
        observeViewModel()

        viewModel.fetchApprovedItems()
    }

    private fun setupRecyclerView() {
        adapter = LibraryAdapter(requireContext(), emptyList(), viewModel) { dataModel ->
            val intent = Intent(requireContext(), LibraryDetailActivity::class.java).apply {
                putExtra("ITEM_ID", dataModel.id)
            }
            startActivity(intent)
        }

        binding.rvLibrary.adapter = adapter
        binding.rvLibrary.layoutManager = LinearLayoutManager(requireContext()) // Default to list layout
    }

    private fun setupLayoutSwitcher() {
        binding.btnList.setOnClickListener {
            isGridLayout = false
            applyLayoutManager()
            adapter.setLayoutType(isGridLayout)
        }

        binding.btnGrid.setOnClickListener {
            isGridLayout = true
            applyLayoutManager()
            adapter.setLayoutType(isGridLayout)
        }
    }

    private fun applyLayoutManager() {
        binding.rvLibrary.layoutManager = if (isGridLayout) {
            GridLayoutManager(requireContext(), 2)
        } else {
            LinearLayoutManager(requireContext())
        }
    }

    private fun observeViewModel() {
        viewModel.items.observe(viewLifecycleOwner, Observer { items ->
            if (items.isEmpty()) {
                Log.d("LibraryFragment", "No items found")
            } else {
                Log.d("LibraryFragment", "Items found: $items")
            }
            adapter.updateData(items)
        })
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}