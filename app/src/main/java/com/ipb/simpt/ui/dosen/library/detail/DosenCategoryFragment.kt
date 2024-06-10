package com.ipb.simpt.ui.dosen.library.detail

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.ipb.simpt.R

class DosenCategoryFragment : Fragment() {

    private val viewModel: DosenCategoryViewModel by activityViewModels()
    private lateinit var itemId: String
    private lateinit var itemName: String
    private lateinit var categoryName: String
    private lateinit var categoryValue: String

    private lateinit var etCategoryName: EditText
    private lateinit var saveButton: Button
    private lateinit var editButton: Button
    private lateinit var deleteButton: ImageView

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
        return inflater.inflate(R.layout.fragment_dosen_category, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        etCategoryName = view.findViewById(R.id.et_category_name)
        deleteButton = view.findViewById(R.id.iv_delete)
        editButton = view.findViewById(R.id.btn_edit)
        saveButton = view.findViewById(R.id.btn_save)

        // Set the category name
        etCategoryName.setText(itemName)

        // Initial state: disable save button
        enableEditText(false)

        // Handle edit button click
        editButton.setOnClickListener {
            enableEditText(true)
        }

        // Handle save button click
        saveButton.setOnClickListener {
            // Save the updated category name
            val updatedItemName = etCategoryName.text.toString()
            if (updatedItemName.isNotBlank()) {
                viewModel.updateCategoryName(categoryName, categoryValue, itemId, updatedItemName)

                // Refresh the RecyclerView
                (activity as DosenCategoryDetailActivity).refreshRecyclerView()

                enableEditText(false)
            } else {
                Toast.makeText(context, "Category name cannot be empty", Toast.LENGTH_SHORT).show()
            }
        }

        // Handle delete button click
        deleteButton.setOnClickListener {
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

        // Observe update result
        viewModel.updateResult.observe(viewLifecycleOwner) { result ->
            result.onSuccess {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                requireActivity().setResult(Activity.RESULT_OK)
                requireActivity().finish() // Close detail activity
            }.onFailure {
                Toast.makeText(requireContext(), it.localizedMessage, Toast.LENGTH_SHORT).show()
            }
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

    private fun enableEditText(enable: Boolean) {
        etCategoryName.isEnabled = enable
        saveButton.isEnabled = enable
    }
}