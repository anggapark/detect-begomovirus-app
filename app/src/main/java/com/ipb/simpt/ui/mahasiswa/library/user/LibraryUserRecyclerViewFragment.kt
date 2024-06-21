package com.ipb.simpt.ui.mahasiswa.library.user

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.ipb.simpt.databinding.FragmentLibraryUserRecyclerViewBinding
import com.ipb.simpt.model.DataModel
import com.ipb.simpt.ui.adapter.LibraryAdapter
import com.ipb.simpt.ui.mahasiswa.library.LibraryDetailActivity
import com.ipb.simpt.ui.mahasiswa.library.LibraryViewModel

class LibraryUserRecyclerViewFragment : Fragment() {

    private var _binding: FragmentLibraryUserRecyclerViewBinding? = null
    private val binding get() = _binding!!

    private lateinit var dataList: ArrayList<DataModel>
    private lateinit var viewModel: LibraryViewModel
    private lateinit var adapter: LibraryAdapter
    private lateinit var userId: String

    companion object {
        private const val ARG_USER_ID = "USER_ID"

        fun newInstance(userId: String) =
            LibraryUserRecyclerViewFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_USER_ID, userId)
                }
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            userId = it.getString(ARG_USER_ID) ?: ""
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLibraryUserRecyclerViewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this).get(LibraryViewModel::class.java)

        setupRecyclerView()
        setupSearchFilter()

        viewModel.fetchDataByStatusAndUserId(userId)
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

        binding.rvData.adapter = adapter
        binding.rvData.layoutManager =
            LinearLayoutManager(requireContext()) // Default to list layout

        // Observe data changes
        viewModel.items.observe(viewLifecycleOwner, Observer { items ->
            if (items != null) {
                dataList.clear()
                dataList.addAll(items)
                adapter.updateData(dataList)
                showLoading(false)
            } else {
                showLoading(false)
            }
        })
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

}