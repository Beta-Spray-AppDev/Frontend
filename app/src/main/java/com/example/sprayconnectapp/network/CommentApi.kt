package com.example.sprayconnectapp.network

import com.example.sprayconnectapp.data.dto.CommentDto
import com.example.sprayconnectapp.data.dto.CreateCommentRequest
import retrofit2.Response
import retrofit2.http.*
import java.util.UUID

interface CommentApi {
    @POST("api/comments")
    suspend fun create(@Body body: CreateCommentRequest): Response<CommentDto>

    @GET("api/comments/boulder/{boulderId}")
    suspend fun listByBoulder(@Path("boulderId") boulderId: UUID): Response<List<CommentDto>>

    @DELETE("api/comments/{commentId}")
    suspend fun delete(@Path("commentId") commentId: UUID): Response<Unit>
}