package com.example.sprayconnectapp.data.dto

import java.util.UUID

data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String
)

data class LoginRequest(
    val username: String,
    val password: String
)

data class UserProfile(
    val id: UUID,
    val username: String,
    val email: String?,
    val createdAt: Long
)


data class UpdateProfileRequest(
    val username: String? = null,
    val email: String? = null,
    val password: String? = null
)