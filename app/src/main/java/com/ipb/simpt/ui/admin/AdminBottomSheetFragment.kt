package com.ipb.simpt.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.ipb.simpt.databinding.FragmentAdminBottomSheetBinding

class AdminBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentAdminBottomSheetBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AdminViewModel by activityViewModels()

    private lateinit var userId: String

    companion object {
        private const val ARG_USER_ID = "user_id"

        fun newInstance(userId: String): AdminBottomSheetFragment {
            val fragment = AdminBottomSheetFragment()
            val args = Bundle()
            args.putString(ARG_USER_ID, userId)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {
            userId = it.getString(ARG_USER_ID) ?: ""
        }

        binding.btnUser.setOnClickListener {
            viewModel.updateUserType(userId, "user")
            dismiss()
        }

        binding.btnDosen.setOnClickListener {
            viewModel.updateUserType(userId, "dosen")
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}