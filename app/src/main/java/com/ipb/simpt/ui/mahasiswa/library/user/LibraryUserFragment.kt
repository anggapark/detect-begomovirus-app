package com.ipb.simpt.ui.mahasiswa.library.user

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.ipb.simpt.R
import com.ipb.simpt.databinding.FragmentLibraryUserBinding
import com.ipb.simpt.ui.mahasiswa.library.LibraryViewModel

class LibraryUserFragment : Fragment() {

    private var _binding: FragmentLibraryUserBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: LibraryViewModel
    private lateinit var userId: String


    companion object {
        private const val ARG_USER_ID = "USER_ID"

        fun newInstance(userId: String) =
            LibraryUserFragment().apply {
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
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLibraryUserBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize the viewModel here
        viewModel = ViewModelProvider(this).get(LibraryViewModel::class.java)

        updateUserUI(userId)
    }

    private fun updateUserUI(uid: String) {
        viewModel.fetchUserDetails(uid)
        viewModel.user.observe(viewLifecycleOwner, Observer { user ->
            if (user != null) {
                binding.tvName.text = user.userName
                binding.tvNim.text = user.userNim
                Glide.with(this)
                    .load(user.profileImage)
                    .placeholder(R.drawable.ic_profile) // Placeholder icon
                    .transform(CircleCrop())
                    .into(binding.ivProfile)
            }
        })
    }
}