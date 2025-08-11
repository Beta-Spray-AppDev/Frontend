package com.example.sprayconnectapp.network

import com.example.sprayconnectapp.data.dto.BoulderDTO
import com.example.sprayconnectapp.data.dto.CreateBoulderRequest
import retrofit2.http.POST
import retrofit2.http.Body
import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path
import java.util.UUID


interface BoulderApi {
    @POST("boulders")
    suspend fun createBoulder(@Body request: CreateBoulderRequest): Response<Unit>

    @GET("boulders/spraywall/{spraywallId}")
    suspend fun getBouldersBySpraywall(@Path("spraywallId") spraywallId: UUID): Response<List<BoulderDTO>>

    @GET("boulders/mine")
    suspend fun getMyBoulders(): Response<List<BoulderDTO>>


    @GET("boulders/{boulderId}")
    suspend fun getBoulderById(
        @Path("boulderId") boulderId: UUID
    ): Response<BoulderDTO>

    @PUT("boulders/{boulderId}")
    suspend fun updateBoulder(
        @Path("boulderId") boulderId: String,
        @Body request: BoulderDTO
    ): Response<BoulderDTO>


    // BoulderApi
    @DELETE("boulders/{boulderId}")
    suspend fun deleteBoulder(@Path("boulderId") boulderId: String): Response<Unit>



}