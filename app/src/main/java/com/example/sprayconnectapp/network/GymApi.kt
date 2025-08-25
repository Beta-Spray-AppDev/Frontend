package com.example.sprayconnectapp.network

import com.example.sprayconnectapp.data.dto.CreateGymDTO
import retrofit2.Response
import retrofit2.http.GET
import com.example.sprayconnectapp.data.dto.Gym
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Endpunkte für Gyms (listen/anlegen).
 */

interface GymApi {

    @GET("api/gyms")
    suspend fun getAllGyms(): Response<List<Gym>>
    @GET("ping")
    suspend fun ping(): Response<Unit>
    @POST("api/gyms")
    suspend fun createGym(@Body gym: CreateGymDTO): Response<Unit>




}