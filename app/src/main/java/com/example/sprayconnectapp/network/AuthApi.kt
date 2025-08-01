package com.example.sprayconnectapp.network

import com.example.sprayconnectapp.data.LoginRequest
import com.example.sprayconnectapp.data.RegisterRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {

    @POST("/auth/register")
    suspend fun register(
        @Body request: RegisterRequest
    ): Response<Void> 

    @POST("/auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<String> // Antwort vom Server
}