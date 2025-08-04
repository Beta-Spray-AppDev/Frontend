package com.example.sprayconnectapp.network

import retrofit2.Response
import retrofit2.http.GET
import com.example.sprayconnectapp.data.dto.Gym


interface GymApi {

    @GET("api/gyms")
    suspend fun getAllGyms(): Response<List<Gym>>

}