package com.example.sprayconnectapp.data.dto

data class TokenResponse(
    val accessToken: String,
    val accessTokenExpiresIn: Long,
    val refreshToken: String,
    val refreshTokenExpiresIn: Long
)

data class RefreshRequest(val refreshToken: String)
data class LogoutRequest(val refreshToken: String)
