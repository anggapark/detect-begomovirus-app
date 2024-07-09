package com.ipb.simpt.ui.mahasiswa.profile

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.ipb.simpt.R
import com.ipb.simpt.databinding.FragmentProfileBinding
import com.ipb.simpt.ui.auth.splash.WelcomeActivity
import com.ipb.simpt.utils.Extensions.toast
import com.ipb.simpt.utils.FileHelper

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var profileViewModel: ProfileViewModel

    private var imgUri: Uri? = null

    companion object {
        private const val TAG = "ProfileFragment"
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
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
                    binding.tvEmail.text = user.email
                    Glide.with(this)
                        .load(user.profileImage)
                        .placeholder(R.drawable.ic_profile) // Placeholder icon
                        .into(binding.ivProfile)
                }
            }
        }
    }

    private fun setupAction() {
        binding.tvName.setOnClickListener {
            navigateToEditProfile("userName")
        }

        binding.tvNim.setOnClickListener {
            navigateToEditProfile("userNim")
        }

        binding.tvEmail.setOnClickListener {
            // TODO: email change also change user authentication
            // navigateToEditProfile("email")
        }

        binding.tvProfileChange.setOnClickListener {
            imagePickIntent()
        }

        binding.tvPasswordChange.setOnClickListener {
            // TODO: this operation is sensitive and requires recent authentication updating password
             navigateToChangePassword()
        }

        binding.btnLogout.setOnClickListener{
            signOut()
        }
    }

    private fun imagePickIntent() {
        Log.d(TAG, "imagePickIntent: starting image pick intent")

        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        imageActivityResultLauncher.launch(intent)
    }

    private val imageActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
        ActivityResultCallback { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                Log.d(TAG, "Image Picked")
                imgUri = result.data?.data

                // Convert the image URI to a File object
                val imgFile = FileHelper.uriToFile(imgUri!!, requireContext())

                // Reduce the size of the image file
                val reducedImgFile = FileHelper.reduceFileImage(imgFile)

                // Update imgUri to point to the reduced image file
                imgUri = Uri.fromFile(reducedImgFile)

                // Set the picked image to your ImageView
                binding.ivProfile.setImageURI(imgUri)

                // Upload the new profile picture to Firebase Storage
                profileViewModel.uploadImgToStorage(imgUri!!)
            } else {
                Log.d(TAG, "Image pick cancelled")
                Toast.makeText(requireContext(), "Cancelled", Toast.LENGTH_SHORT).show()
            }
        }
    )


    private fun navigateToEditProfile(field: String) {
        val bundle = Bundle().apply {
            putString("field", field)
        }
        findNavController().navigate(R.id.navigation_profile_edit, bundle)
    }

    private fun navigateToChangePassword() {
        val bundle = Bundle().apply {
            putBoolean("isPasswordChange", true)
        }
        findNavController().navigate(R.id.navigation_profile_edit, bundle)
    }

    private fun signOut() {
        firebaseAuth.signOut()
        startActivity(Intent(requireActivity(), WelcomeActivity::class.java))
        Toast.makeText(requireContext(), "Signed Out", Toast.LENGTH_SHORT).show()
        requireActivity().finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
