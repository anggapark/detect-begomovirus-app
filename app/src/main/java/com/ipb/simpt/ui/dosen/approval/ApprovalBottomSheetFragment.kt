package com.ipb.simpt.ui.dosen.approval

import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.ipb.simpt.R
import com.ipb.simpt.databinding.FragmentApprovalBottomSheetBinding

class ApprovalBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentApprovalBottomSheetBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ApprovalViewModel by activityViewModels()
    private lateinit var itemId: String
    private lateinit var userType: String

    companion object {
        private const val ARG_ITEM_ID = "item_id"
        private const val ARG_USER_TYPE = "user_type"

        fun newInstance(itemId: String, userType: String): ApprovalBottomSheetFragment {
            val fragment = ApprovalBottomSheetFragment()
            val args = Bundle()
            args.putString(ARG_ITEM_ID, itemId)
            args.putString(ARG_USER_TYPE, userType)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentApprovalBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {
            itemId = it.getString(ARG_ITEM_ID) ?: ""
            userType = it.getString(ARG_USER_TYPE) ?: ""
        }

        if (userType == "user") {
            setupForUser()
        } else {
            setupForDosen()
        }
    }

    private fun setupForDosen() {
        binding.switchComment.setOnCheckedChangeListener { _, isChecked ->
            binding.tilComment.visibility = if (isChecked) View.VISIBLE else View.GONE
        }

        binding.btnApprove.setOnClickListener {
            val comment = if (binding.switchComment.isChecked) {
                binding.edAddComment.text.toString()
            } else {
                "Tidak ada tanggapan"
            }

            if (binding.switchComment.isChecked && comment.isEmpty()) {
                Toast.makeText(requireContext(), "Komentar tidak boleh kosong", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.approveData(itemId, comment)
            parentFragmentManager.setFragmentResult("approvalResult", Bundle().apply { putBoolean("dataChanged", true) })
            Toast.makeText(requireContext(), "Data approved successfully!", Toast.LENGTH_SHORT).show()
            dismiss()
        }

        binding.btnReject.setOnClickListener {
            val comment = if (binding.switchComment.isChecked) {
                binding.edAddComment.text.toString()
            } else {
                "Tidak ada tanggapan"
            }

            if (binding.switchComment.isChecked && comment.isEmpty()) {
                Toast.makeText(requireContext(), "Komentar tidak boleh kosong", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.rejectData(itemId, comment)
            parentFragmentManager.setFragmentResult("approvalResult", Bundle().apply { putBoolean("dataChanged", true) })
            Toast.makeText(requireContext(), "Data rejected successfully!", Toast.LENGTH_SHORT).show()
            dismiss()
        }
    }

    private fun setupForUser() {
        binding.tvComment.text = getString(R.string.hint_response)
        binding.btnApprove.visibility = View.GONE
        binding.btnReject.visibility = View.GONE
        binding.switchComment.visibility = View.GONE
        binding.tilComment.visibility = View.VISIBLE
        binding.edAddComment.isEnabled = false

        viewModel.fetchDataResponse(itemId) { comment ->
            if (comment.isEmpty()) {
                binding.edAddComment.text = Editable.Factory.getInstance().newEditable("Belum ada tanggapan")
            } else {
                binding.edAddComment.text = Editable.Factory.getInstance().newEditable(comment)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
