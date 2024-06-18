package com.ipb.simpt.ui.mahasiswa.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.ipb.simpt.databinding.FragmentEditProfileBinding

class EditProfileFragment : Fragment() {

    private var _binding: FragmentEditProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var profileViewModel: ProfileViewModel
    private lateinit var firebaseAuth: FirebaseAuth

    private var field: String? = null
    private var isPasswordChange = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firebaseAuth = FirebaseAuth.getInstance()
        profileViewModel = ViewModelProvider(this).get(ProfileViewModel::class.java)

        field = arguments?.getString("field")
        isPasswordChange = arguments?.getBoolean("isPasswordChange", false) ?: false

        fieldCheck()
        setupAction()
    }

    private fun fieldCheck() {
        if (isPasswordChange) {
            binding.textTitle.text = "Change Password"
            binding.editText.visibility = View.GONE
            binding.etPasswordLayout.visibility = View.VISIBLE
            binding.etConfirmLayout.visibility = View.VISIBLE
        } else {
            when (field) {
                "userName" -> binding.textTitle.text = "Edit Nama"
                "userNim" -> binding.textTitle.text = "Edit NIM"
                "email" -> binding.textTitle.text = "Edit Email"
            }
            observeUserProfile()
            fetchUserProfile()
        }
    }

    private fun setupAction() {
        binding.buttonSave.setOnClickListener {
            if (isPasswordChange) {
                changePassword()
            } else {
                saveChanges(field)
            }
        }
    }

    private fun observeUserProfile() {
        profileViewModel.user.observe(viewLifecycleOwner, Observer { user ->
            user?.let {
                when (field) {
                    "userName" -> binding.editText.setText(it.userName)
                    "userNim" -> binding.editText.setText(it.userNim)
                    "email" -> binding.editText.setText(it.email)
                }
            }
        })
    }

    private fun fetchUserProfile() {
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser != null) {
            profileViewModel.fetchUserDetails(firebaseUser.uid)
        }
    }

    private fun saveChanges(field: String?) {
        val newValue = binding.editText.text.toString()
        val firebaseUser = firebaseAuth.currentUser

        if (firebaseUser != null && field != null) {
            profileViewModel.updateUserField(firebaseUser.uid, field, newValue) {
                findNavController().navigateUp()
            }
        }
    }

    private fun changePassword() {
        val newPassword = binding.etPassword.text.toString()
        val confirmPassword = binding.etConfirm.text.toString()

        if (newPassword == confirmPassword) {
            profileViewModel.changePassword(newPassword) { isSuccess, errorMessage ->
                if (isSuccess) {
                    findNavController().navigateUp()
                } else {
                    Toast.makeText(requireContext(), "Error updating password: $errorMessage", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(requireContext(), "Passwords do not match", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
