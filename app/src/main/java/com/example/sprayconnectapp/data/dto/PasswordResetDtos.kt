package com.example.sprayconnectapp.data.dto


data class ForgotPasswordRequest(
    val email: String
)


data class ResetPasswordRequest(
    val token: String,
    val newPassword: String
)


data class ValidateTokenResponse(
    val valid: Boolean
)