package com.example.sprayconnectapp.network

import com.example.sprayconnectapp.data.dto.LoginRequest
import com.example.sprayconnectapp.data.dto.LogoutRequest
import com.example.sprayconnectapp.data.dto.RefreshRequest
import com.example.sprayconnectapp.data.dto.RegisterRequest
import com.example.sprayconnectapp.data.dto.TokenResponse
import com.example.sprayconnectapp.data.dto.UpdateProfileRequest
import com.example.sprayconnectapp.data.dto.UserProfile
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT

/**
 * Authentifizierungsendpunkte:
 * - Registrierung/Anmeldung
 * - Profil lesen/aktualisieren
 */

interface AuthApi {

    @POST("/auth/register")
    suspend fun register(
        @Body request: RegisterRequest
    ): Response<Void>

    @POST("/auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<TokenResponse> // Antwort vom Server -> Token

    @POST("/auth/refresh")
    suspend fun refresh(@Body req: RefreshRequest): Response<TokenResponse>

    @POST("/auth/logout")
    suspend fun logout(@Body request: LogoutRequest): Response<Void>


    @GET("/auth/profile")
    suspend fun getProfile(): Response<UserProfile>

    @PUT("/auth/profile")
    suspend fun updateProfile(@Body dto: UpdateProfileRequest): Response<UserProfile>


}