package com.ipb.simpt.ui.dosen.library.detail

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.ipb.simpt.databinding.FragmentDosenCategoryBinding
import com.ipb.simpt.ui.dosen.library.DosenCategoryViewModel

class DosenCategoryFragment : Fragment() {


    private var _binding: FragmentDosenCategoryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DosenCategoryViewModel by activityViewModels()
    private lateinit var itemId: String
    private lateinit var itemName: String
    private lateinit var categoryName: String
    private lateinit var categoryValue: String

    companion object {
        private const val ARG_ITEM_ID = "ITEM_ID"
        private const val ARG_ITEM_NAME = "ITEM_NAME"
        private const val ARG_CATEGORY_NAME = "CATEGORY_NAME"
        private const val ARG_CATEGORY_VALUE = "CATEGORY_VALUE"

        fun newInstance(
            itemId: String,
            itemName: String,
            categoryName: String,
            categoryValue: String
        ) = DosenCategoryFragment().apply {
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
            categoryValue = it.getString(ARG_CATEGORY_VALUE) ?: ""
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDosenCategoryBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set the category name
        binding.tvCategoryName.setText(itemName)
        binding.tvCategoryName.setOnClickListener {
            val intent =
                Intent(requireContext(), DosenCategoryDetailEditActivity::class.java).apply {
                    putExtra("ITEM_ID", itemId)
                    putExtra("ITEM_NAME", itemName)
                    putExtra("CATEGORY_NAME", categoryName)
                    putExtra("CATEGORY_VALUE", categoryValue)
                }
            startActivity(intent)
        }

        // Handle delete button click
        binding.ivDelete.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Confirm Deletion")
                .setMessage("Are you sure you want to delete this category?")
                .setPositiveButton("Yes") { dialog, _ ->
                    viewModel.deleteCategory(categoryName, categoryValue, itemId)
                    dialog.dismiss()
                }
                .setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                }
                .create()
                .show()
        }

        // Observe delete result
        viewModel.deleteResult.observe(viewLifecycleOwner, Observer { result ->
            result.onSuccess {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                requireActivity().setResult(Activity.RESULT_OK) // Set result OK to trigger list update
                requireActivity().finish() // Close detail activity
            }.onFailure {
                Toast.makeText(requireContext(), it.localizedMessage, Toast.LENGTH_SHORT).show()
            }
        })
    }
}