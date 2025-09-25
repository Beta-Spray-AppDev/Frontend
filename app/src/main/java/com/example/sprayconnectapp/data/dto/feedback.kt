package com.example.sprayconnectapp.data.dto.feedback

data class CreateFeedbackDto(
    val stars: Int,
    val message: String?,
    val username: String,
    val appVersion: String?,
    val deviceInfo: String?
)

data class FeedbackDto(
    val id: String,
    val username: String,
    val stars: Int,
    val message: String?,
    val createdAt: Long,
    val appVersion: String?,
    val deviceInfo: String?
)
