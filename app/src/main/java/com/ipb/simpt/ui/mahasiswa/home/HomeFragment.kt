package com.ipb.simpt.ui.mahasiswa.home

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.google.firebase.auth.FirebaseAuth
import com.ipb.simpt.R
import com.ipb.simpt.databinding.FragmentHomeBinding
import com.ipb.simpt.ui.dosen.approval.ApprovalActivity
import com.ipb.simpt.ui.mahasiswa.add.AddDataActivity
import com.ipb.simpt.ui.mahasiswa.mydata.MyDataActivity
import com.ipb.simpt.ui.mahasiswa.profile.ProfileViewModel

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var profileViewModel: ProfileViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firebaseAuth = FirebaseAuth.getInstance()
        profileViewModel = ViewModelProvider(this).get(ProfileViewModel::class.java)

        setupUserProfile()
        setupAction()
    }

    private fun setupUserProfile() {
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser != null) {
            profileViewModel.fetchUserDetails(firebaseUser.uid)
            profileViewModel.user.observe(viewLifecycleOwner) { user ->
                if (user != null) {
                    binding.tvName.text = user.userName
                    binding.tvNim.text = user.userNim
                    Glide.with(this)
                        .load(user.profileImage)
                        .placeholder(R.drawable.ic_profile) // Placeholder icon
                        .transform(CircleCrop())
                        .into(binding.ivProfile)
                }
            }
        }
    }

    private fun setupAction() {
        binding.cvAdd.setOnClickListener {
            startActivity(Intent(requireContext(), AddDataActivity::class.java))
        }

        binding.cvData.setOnClickListener {
            startActivity(Intent(requireContext(), MyDataActivity::class.java))
        }

        binding.cvSubmission.setOnClickListener {
            val intent = Intent(requireContext(), ApprovalActivity::class.java).apply {
                putExtra("USER_TYPE", "user")
            }
            startActivity(intent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

// TODO: (
//  7. Filter untuk LibraryFragment dan MyData
//  10. REDESIGN
//  )

// TODO OPSIONAL (
//  1. Batas Authentikasi login
//  4. Intent ke DosenCategoryDetailActivity setelah edit dari DosenCategoryDetailEditActivity
//  8. Intent AddCategoryActivity ke DosenCategoryList
//  12. Add UID of commentator
//  )

// TODO KELAR (
//  2. Submission
//  3. Intent AddDataActivity ke Submission
//  5. Benahi layout ApprovalDetailActivity
//  6. Comment system di ApprovalDetailActivity
//  9. Benahi NavGraph untuk halaman search
//  11. Reset form di AddData setelah submit
//  )