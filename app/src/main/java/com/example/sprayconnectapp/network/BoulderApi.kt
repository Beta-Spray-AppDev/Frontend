package com.example.sprayconnectapp.network

import com.example.sprayconnectapp.data.dto.BoulderDTO
import com.example.sprayconnectapp.data.dto.CreateBoulderRequest
import com.example.sprayconnectapp.data.dto.TickCreateRequest
import com.example.sprayconnectapp.data.dto.TickDto
import com.example.sprayconnectapp.data.dto.TickWithBoulderDto
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
    @POST("/api/boulders")
     suspend fun createBoulder(@Body request: CreateBoulderRequest): Response<BoulderDTO>

    @GET("/api/boulders/spraywall/{spraywallId}")
    suspend fun getBouldersBySpraywall(@Path("spraywallId") spraywallId: UUID): Response<List<BoulderDTO>>

    @GET("/api/boulders/mine") suspend fun getMyBoulders(): Response<List<BoulderDTO>>

    @GET("/api/boulders/{boulderId}")
    suspend fun getBoulderById(@Path("boulderId") boulderId: UUID?): Response<BoulderDTO>

    @PUT("/api/boulders/{boulderId}")
    suspend fun updateBoulder(@Path("boulderId") boulderId: String, @Body request: BoulderDTO): Response<BoulderDTO>


    /** Löscht Boulder serverseitig */
    @DELETE("/api/boulders/{boulderId}")
    suspend fun deleteBoulder(@Path("boulderId") boulderId: String): Response<Unit>


    /** Alle eigenen Ticks (als BoulderDTO-Liste) */
    @GET("/api/boulders/ticks/mine")
    suspend fun getMyTickedBoulders(): Response<List<TickWithBoulderDto>>


    /** Untick: entfernt den Tick des eingeloggten Users für diesen Boulder */
    @DELETE("/api/boulders/{boulderId}/ticks")
    suspend fun deleteTick(@Path("boulderId") boulderId: String): Response<Unit>



    /**
     * Untick per TICK-ID – funktioniert auch, wenn der Boulder gelöscht wurde
     * (boulder_id in der DB ist dann NULL).
     */

    @DELETE("/api/boulders/ticks/{tickId}")
    suspend fun deleteTickById(@Path("tickId") tickId: String): Response<Unit>



    @POST("/api/boulders/{boulderId}/ticks")
    suspend fun tickBoulder(
        @Path("boulderId") boulderId: String,
        @Body body: TickCreateRequest = TickCreateRequest()
    ): Response<TickDto>





}
