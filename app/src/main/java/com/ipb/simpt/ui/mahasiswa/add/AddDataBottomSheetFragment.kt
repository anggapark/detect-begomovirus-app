package com.ipb.simpt.ui.mahasiswa.add

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.ipb.simpt.databinding.FragmentAddDataBottomSheetBinding
import com.ipb.simpt.repository.DataRepository

class AddDataBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentAddDataBottomSheetBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AddDataViewModel by activityViewModels()
    private var itemId: String? = null
    private var fromOtherActivity: Boolean = false

    companion object {
        private const val ARG_ITEM_ID = "item_id"
        private const val ARG_FROM_OTHER_ACTIVITY = "from_other_activity"
        const val TAG = "AddDataBottomSheetFragment"
        private const val REQUEST_PICK_IMAGE = 123

        fun newInstance(itemId: String, fromOtherActivity: Boolean = false): AddDataBottomSheetFragment {
            val fragment = AddDataBottomSheetFragment()
            val args = Bundle().apply {
                putString(ARG_ITEM_ID, itemId)
                putBoolean(ARG_FROM_OTHER_ACTIVITY, fromOtherActivity)
            }
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddDataBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {
            itemId = it.getString(ARG_ITEM_ID) ?: ""
            fromOtherActivity = it.getBoolean(ARG_FROM_OTHER_ACTIVITY, false)
        }

        // Check if itemId is not null or empty before fetching data
        itemId?.takeIf { it.isNotEmpty() }?.let { nonNullItemId ->
            showLoading(true)
            val repository = DataRepository()
            repository.fetchItemDetail(nonNullItemId) { dataModel ->
                if (isAdded) {
                    Glide.with(requireContext())
                        .load(dataModel.datasetUrl)
                        .into(binding.ivPreview)

                    showLoading(false)
                }
            }
        } ?: run {
            showLoading(false)
        }

        if (fromOtherActivity) {
            binding.btnAddImage.visibility = View.GONE
        }

        val imageClickListener = View.OnClickListener {
            handleImagePick()
        }

        binding.btnAddImage.setOnClickListener(imageClickListener)
        binding.cvPreview.setOnClickListener(imageClickListener)

        viewModel.bottomSheetImgUri.observe(viewLifecycleOwner, Observer { uri ->
            uri?.let {
                if (isAdded) { // Check if the fragment is still added
                    binding.ivPreview.setImageURI(it)
                }
            }
        })
    }

    private fun handleImagePick() {
        imagePickIntent()
        binding.btnAddImage.isEnabled = false
        binding.cvPreview.isEnabled = false
    }

    private fun imagePickIntent() {
        Log.d(TAG, "imagePickIntent: starting image pick intent")

        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(intent, REQUEST_PICK_IMAGE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_PICK_IMAGE && resultCode == RESULT_OK) {
            Log.d(TAG, "Image Picked")
            val uri = data?.data
            uri?.let {
                viewModel.setBottomSheetImgUri(it)
            }
        } else {
            Log.d(TAG, "Image pick cancelled")
            Toast.makeText(requireContext(), "Cancelled", Toast.LENGTH_SHORT).show()
        }

        // Re-enable the buttons after the image pick activity is finished
        binding.btnAddImage.isEnabled = true
        binding.cvPreview.isEnabled = true
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()

        // Clear any pending image requests when the view is destroyed
        Glide.with(requireContext()).clear(binding.ivPreview)
    }
}