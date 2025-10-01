package com.example.sprayconnectapp.data.dto

import java.util.UUID

data class CommentDto(
    val id: UUID,
    val content: String,
    val created: Long,
    val boulderId: UUID,
    val userId: UUID,
    val createdByUsername: String?
)

data class CreateCommentRequest(
    val content: String,
    val boulderId: UUID
)
