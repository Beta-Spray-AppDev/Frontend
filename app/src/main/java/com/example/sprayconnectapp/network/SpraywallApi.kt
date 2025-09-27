package com.example.sprayconnectapp.network

import com.example.sprayconnectapp.data.dto.SpraywallDTO
import retrofit2.Response
import retrofit2.http.*
import java.util.UUID

/**
 * Endpunkte für Spraywalls
 */
interface SpraywallApi {

    // Neu: archivierte per Query umschalten (default = aktiv)
    @GET("api/spraywalls/gym/{gymId}")
    suspend fun getSpraywallsByGym(
        @Path("gymId") gymId: UUID,
        @Query("archived") archived: Boolean = false
    ): Response<List<SpraywallDTO>>

    @POST("api/spraywalls")
    suspend fun createSpraywall(@Body dto: SpraywallDTO): Response<SpraywallDTO>

    @GET("api/spraywalls/{id}")
    suspend fun getSpraywallById(@Path("id") id: UUID): Response<SpraywallDTO>

    // Neu: idempotentes Archiv-Toggle
    @PATCH("api/spraywalls/gym/{gymId}/{spraywallId}/archive")
    suspend fun setArchived(
        @Path("gymId") gymId: UUID,
        @Path("spraywallId") spraywallId: UUID,
        @Query("archived") archived: Boolean
    ): Response<Unit>

}
