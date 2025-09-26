package com.example.sprayconnectapp.network

import com.example.sprayconnectapp.data.dto.BoulderDTO
import com.example.sprayconnectapp.data.dto.CreateBoulderRequest
import com.example.sprayconnectapp.data.dto.TickDto
import retrofit2.http.POST
import retrofit2.http.Body
import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path
import java.util.UUID


/**
 * Endpunkte rund um Boulder
 */
interface BoulderApi {
    @POST("boulders")
     suspend fun createBoulder(@Body request: CreateBoulderRequest): Response<BoulderDTO>

    @GET("boulders/spraywall/{spraywallId}")
    suspend fun getBouldersBySpraywall(@Path("spraywallId") spraywallId: UUID): Response<List<BoulderDTO>>

    @GET("boulders/mine") suspend fun getMyBoulders(): Response<List<BoulderDTO>>

    @GET("boulders/{boulderId}")
    suspend fun getBoulderById(@Path("boulderId") boulderId: UUID?): Response<BoulderDTO>

    @PUT("boulders/{boulderId}")
    suspend fun updateBoulder(@Path("boulderId") boulderId: String, @Body request: BoulderDTO): Response<BoulderDTO>


    /** Markiert Boulder als getickt (Begehung) und liefert Tick-Info zurück */
    @POST("boulders/{boulderId}/ticks")
    suspend fun tickBoulder(@Path("boulderId") boulderId: String): Response<TickDto>

    /** Löscht Boulder serverseitig */
    @DELETE("boulders/{boulderId}")
    suspend fun deleteBoulder(@Path("boulderId") boulderId: String): Response<Unit>


    /** Alle eigenen Ticks (als BoulderDTO-Liste) */
    @GET("boulders/ticks/mine")
    suspend fun getMyTickedBoulders(): Response<List<BoulderDTO>>


    /** Untick: entfernt den Tick des eingeloggten Users für diesen Boulder */
    @DELETE("boulders/{boulderId}/ticks")
    suspend fun deleteTick(@Path("boulderId") boulderId: String): Response<Unit>





}
