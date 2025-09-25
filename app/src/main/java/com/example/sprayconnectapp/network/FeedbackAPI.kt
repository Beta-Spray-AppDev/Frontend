package com.example.sprayconnectapp.network

import com.example.sprayconnectapp.data.dto.feedback.CreateFeedbackDto
import com.example.sprayconnectapp.data.dto.feedback.FeedbackDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface FeedbackApi {

    @POST("api/feedback")
    suspend fun createFeedback(@Body dto: CreateFeedbackDto): Response<FeedbackDto>

    @GET("api/feedback")
    suspend fun getAllFeedback(): Response<List<FeedbackDto>>
}
