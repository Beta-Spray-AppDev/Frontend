package com.example.sprayconnectapp.network

import com.example.sprayconnectapp.data.dto.ForgotPasswordRequest
import com.example.sprayconnectapp.data.dto.LoginRequest
import com.example.sprayconnectapp.data.dto.LogoutRequest
import com.example.sprayconnectapp.data.dto.RefreshRequest
import com.example.sprayconnectapp.data.dto.RegisterRequest
import com.example.sprayconnectapp.data.dto.ResetPasswordRequest
import com.example.sprayconnectapp.data.dto.TokenResponse
import com.example.sprayconnectapp.data.dto.UpdateProfileRequest
import com.example.sprayconnectapp.data.dto.UserProfile
import com.example.sprayconnectapp.data.dto.ValidateTokenResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Query

/**
 * Authentifizierungsendpunkte:
 * - Registrierung/Anmeldung
 * - Profil lesen/aktualisieren
 */

interface AuthApi {

    @POST("/api/auth/register")
    suspend fun register(
        @Body request: RegisterRequest
    ): Response<Void>

    @POST("/api/auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<TokenResponse> // Antwort vom Server -> Token

    @POST("/api/auth/refresh")
    suspend fun refresh(@Body req: RefreshRequest): Response<TokenResponse>

    @POST("/api/auth/logout")
    suspend fun logout(@Body request: LogoutRequest): Response<Void>


    @GET("/api/auth/profile")
    suspend fun getProfile(): Response<UserProfile>

    @PUT("/api/auth/profile")
    suspend fun updateProfile(@Body dto: UpdateProfileRequest): Response<UserProfile>


    @POST("/api/auth/password/forgot")
    suspend fun forgotPassword(@Body body: ForgotPasswordRequest): Response<Map<String, String>>


    @GET("/api/auth/password/reset/validate")
    suspend fun validateResetToken(@Query("token") token: String): Response<ValidateTokenResponse>


    @POST("/api/auth/password/reset")
    suspend fun resetPassword(@Body body: ResetPasswordRequest): Response<Map<String, String>>


}