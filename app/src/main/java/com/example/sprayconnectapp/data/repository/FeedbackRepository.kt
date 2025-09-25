package com.example.sprayconnectapp.data.repository

import android.content.Context
import com.example.sprayconnectapp.data.dto.feedback.CreateFeedbackDto
import com.example.sprayconnectapp.data.dto.feedback.FeedbackDto
import com.example.sprayconnectapp.network.RetrofitInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FeedbackRepository(private val context: Context) {

    suspend fun sendFeedback(dto: CreateFeedbackDto): Result<FeedbackDto> = withContext(Dispatchers.IO) {
        try {
            val api = RetrofitInstance.getFeedbackApi(context)
            val response = api.createFeedback(dto)

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("HTTP ${response.code()} ${response.errorBody()?.string()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAllFeedback(): Result<List<FeedbackDto>> = withContext(Dispatchers.IO) {
        try {
            val api = RetrofitInstance.getFeedbackApi(context)
            val response = api.getAllFeedback()

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("HTTP ${response.code()} ${response.errorBody()?.string()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
