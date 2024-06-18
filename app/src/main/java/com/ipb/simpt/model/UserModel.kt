package com.ipb.simpt.model

data class UserModel(
    val uid: String = "",
    val email: String = "",
    val userName: String = "",
    val userNim: String = "",
    val profileImage: String = "",
    val userType: String = "",
    val timestamp: Long = 0
)
