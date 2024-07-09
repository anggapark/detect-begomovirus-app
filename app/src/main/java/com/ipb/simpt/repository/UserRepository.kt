package com.ipb.simpt.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.ipb.simpt.model.DataModel
import com.ipb.simpt.model.UserModel

class UserRepository {

    private val db = FirebaseFirestore.getInstance()

    fun fetchUserDetails(uid: String, callback: (UserModel?) -> Unit) {
        db.collection("Users")
            .document(uid)
            .get()
            .addOnSuccessListener { document ->
                val user = document.toObject(UserModel::class.java)
                callback(user)
            }
            .addOnFailureListener { exception ->
                Log.w("UserRepository", "Error getting document: ", exception)
                callback(null)
            }
    }

    fun fetchUserByType(userType: String, callback: (List<UserModel>) -> Unit) {
        db.collection("Users")
            .whereEqualTo("userType", userType)
            .get()
            .addOnSuccessListener { result ->
                val userList = result.map { document ->
                    document.toObject(UserModel::class.java)
                }
                callback(userList)
            }
            .addOnFailureListener { exception ->
                Log.w("UserRepository", "Error getting document: ", exception)
                callback(emptyList())
            }
    }

    fun updateUserField(uid: String, field: String, newValue: String, callback: () -> Unit) {
        db.collection("Users")
            .document(uid)
            .update(field, newValue)
            .addOnSuccessListener {
                Log.d("UserRepository", "User $field updated successfully")
                callback()
            }
            .addOnFailureListener { exception ->
                Log.e("UserRepository", "Error updating user $field", exception)
                callback()
            }
    }

    fun changePassword(newPassword: String, callback: (Boolean, String?) -> Unit) {
        val user = FirebaseAuth.getInstance().currentUser
        user?.let {
            it.updatePassword(newPassword).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("UserRepository", "User password updated.")
                    callback(true, null)
                } else {
                    Log.e("UserRepository", "Error updating password", task.exception)
                    callback(false, task.exception?.message)
                }
            }
        } ?: run {
            callback(false, "No authenticated user")
        }
    }

    fun updateUserType(userId: String, newType: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        db.collection("Users")
            .document(userId)
            .update("userType", newType)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { e ->
                onFailure(e)
            }
    }
}
