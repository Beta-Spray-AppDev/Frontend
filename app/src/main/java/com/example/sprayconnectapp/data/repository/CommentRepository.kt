package com.example.sprayconnectapp.data.repository

import android.content.Context
import com.example.sprayconnectapp.data.dto.CommentDto
import com.example.sprayconnectapp.data.dto.CreateCommentRequest
import com.example.sprayconnectapp.network.RetrofitInstance
import java.util.UUID

class CommentRepository {
    suspend fun addComment(ctx: Context, boulderId: UUID, content: String): CommentDto? {
        val res = RetrofitInstance.getCommentApi(ctx)
            .create(CreateCommentRequest(content = content, boulderId = boulderId))
        return if (res.isSuccessful) res.body() else null
    }

    suspend fun getComments(ctx: Context, boulderId: UUID): List<CommentDto> {
        val res = RetrofitInstance.getCommentApi(ctx).listByBoulder(boulderId)
        return res.body().orEmpty()
    }

    suspend fun deleteComment(ctx: Context, commentId: UUID): Boolean {
        val res = RetrofitInstance.getCommentApi(ctx).delete(commentId)
        return res.isSuccessful
    }
}
