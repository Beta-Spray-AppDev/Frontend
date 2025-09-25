package com.example.sprayconnectapp.network

import com.example.sprayconnectapp.data.dto.SpraywallDTO
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import java.util.UUID

/**
 * Endpunkte für Spraywalls
 */

interface SpraywallApi {

    @GET("api/spraywalls/gym/{gymId}")
    suspend fun getSpraywallsByGym(@Path("gymId") gymId: UUID): Response<List<SpraywallDTO>>

    @POST("api/spraywalls")
    suspend fun createSpraywall(@Body dto: SpraywallDTO): Response<SpraywallDTO>

    @GET("api/spraywalls/{id}")
    suspend fun getSpraywallById(@Path("id") id: UUID): Response<SpraywallDTO>



}