package com.ipb.simpt.ui.mahasiswa.library.user

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.ipb.simpt.R
import com.ipb.simpt.ui.mahasiswa.library.LibraryViewModel

class LibraryUserFragment : Fragment() {

    private lateinit var viewModel: LibraryViewModel
    private lateinit var userId: String
    private lateinit var ivProfile: ImageView
    private lateinit var tvName: TextView
    private lateinit var tvNim: TextView

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
    ): View? {
        return inflater.inflate(R.layout.fragment_library_user, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ivProfile = view.findViewById(R.id.iv_profile)
        tvName = view.findViewById(R.id.tv_name)
        tvNim = view.findViewById(R.id.tv_nim)

        // Initialize the viewModel here
        viewModel = ViewModelProvider(this).get(LibraryViewModel::class.java)

        updateUserUI(userId)
    }

    private fun updateUserUI(uid: String) {
        viewModel.fetchUserDetails(uid)
        viewModel.user.observe(viewLifecycleOwner, Observer { user ->
            if (user != null) {
                tvName.text = user.userName
                tvNim.text = user.userNim
                Glide.with(this)
                    .load(user.profileImage)
                    .placeholder(R.drawable.ic_profile) // Placeholder icon
                    .transform(CircleCrop())
                    .into(ivProfile)
            }
        })
    }
}