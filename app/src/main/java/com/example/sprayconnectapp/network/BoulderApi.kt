package com.example.sprayconnectapp.network

import com.example.sprayconnectapp.data.dto.CreateBoulderRequest
import retrofit2.http.POST
import retrofit2.http.Body
import retrofit2.Response


interface BoulderApi {
    @POST("boulders")
    suspend fun createBoulder(@Body request: CreateBoulderRequest): Response<Unit>
}