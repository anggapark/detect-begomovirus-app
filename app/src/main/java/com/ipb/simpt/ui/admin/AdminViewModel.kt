package com.ipb.simpt.ui.admin

import android.content.ContentValues.TAG
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ipb.simpt.model.UserModel
import com.ipb.simpt.repository.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

class AdminViewModel : ViewModel() {
    private val userRepository = UserRepository()

    private val _items = MutableLiveData<List<UserModel>>()
    val items: LiveData<List<UserModel>> get() = _items


    private val _user = MutableLiveData<UserModel>()
    val user: LiveData<UserModel> get() = _user

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> get() = _error

    private val viewModelJob = Job()
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    fun fetchUserByType(userType: String) {
        _isLoading.value = true
        coroutineScope.launch {
            try {
                withTimeout(5000L) {
                    userRepository.fetchUserByType(userType) { userList ->
                        _items.postValue(userList)
                        _isLoading.postValue(false)
                        checkEmptyState(userList)
                    }
                }
            } catch (e: TimeoutCancellationException) {
                _isLoading.postValue(false)
                _error.postValue("Request timed out")
                Log.e(TAG, "Fetch users timed out", e)
            } catch (e: Exception) {
                _isLoading.postValue(false)
                _error.postValue(e.message)
                Log.e(TAG, "Error fetching users", e)
            }
        }
    }

    fun fetchUserDetails(uid: String) {
        coroutineScope.launch {
            userRepository.fetchUserDetails(uid) { user ->
                user?.let {
                    _user.value = it
                } ?: run {
                    Log.w(TAG, "User not found with UID: $uid")
                }
            }
        }
    }

    fun updateUserType(userId: String, newType: String) {
        userRepository.updateUserType(userId, newType,
            onSuccess = {
                // Handle success
                Log.d(TAG, "User type updated successfully")
            },
            onFailure = { e ->
                // Handle failure
                Log.w(TAG, "Error updating user type", e)
            }
        )
    }

    private fun checkEmptyState(userList: List<UserModel>) {
        if (userList.isEmpty()) {
            _error.postValue("No items found")
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
}