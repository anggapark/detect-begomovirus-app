package com.ipb.simpt.ui.mahasiswa.profile

import android.app.Application
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.ipb.simpt.model.UserModel
import com.ipb.simpt.repository.UserRepository

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val userRepository = UserRepository()
    private val _user = MutableLiveData<UserModel>()
    val user: LiveData<UserModel> get() = _user

    fun fetchUserDetails(uid: String) {
        userRepository.fetchUserDetails(uid) { user ->
            user?.let {
                _user.value = it
            } ?: run {
                Log.w("ProfileViewModel", "User not found")
            }
        }
    }

    fun updateUserField(uid: String, field: String, newValue: String, callback: () -> Unit) {
        userRepository.updateUserField(uid, field, newValue) {
            fetchUserDetails(uid)
            callback()
        }
    }

    fun changePassword(newPassword: String, callback: (Boolean, String?) -> Unit) {
        userRepository.changePassword(newPassword) { isSuccess, errorMessage ->
            if (isSuccess) {
                callback(true, null)
            } else {
                callback(false, errorMessage)
            }
        }
    }

    fun uploadImgToStorage(imgUri: Uri) {
        val timestamp = System.currentTimeMillis()
        val filePathAndName = "ProfilePicture/$timestamp"
        val storageReference = FirebaseStorage.getInstance().getReference(filePathAndName)
        storageReference.putFile(imgUri)
            .addOnSuccessListener { taskSnapshot ->
                Log.d("ProfileViewModel", "uploadImgToStorage: Image uploaded now getting url")

                val uriTask = taskSnapshot.storage.downloadUrl
                uriTask.addOnSuccessListener { uri ->
                    val uploadedImgUrl = uri.toString()
                    updateProfileImageUrl(uploadedImgUrl)
                }
            }
            .addOnFailureListener { e ->
                Log.d("ProfileViewModel", "uploadImgToStorage: failed to upload due to ${e.message}")
                Toast.makeText(getApplication(), "Failed to upload due to ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateProfileImageUrl(uploadedImgUrl: String) {
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        if (firebaseUser != null) {
            val db = FirebaseFirestore.getInstance()
            db.collection("Users")
                .document(firebaseUser.uid)
                .update("profileImage", uploadedImgUrl)
                .addOnSuccessListener {
                    Log.d("ProfileViewModel", "updateProfileImageUrl: Profile image updated in Firestore")
                    Toast.makeText(getApplication(), "Profile picture updated", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Log.d("ProfileViewModel", "updateProfileImageUrl: failed to update profile image due to ${e.message}")
                    Toast.makeText(getApplication(), "Failed to update profile image due to ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
