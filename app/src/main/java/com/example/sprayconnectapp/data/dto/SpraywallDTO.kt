package com.example.sprayconnectapp.data.dto

import com.google.gson.annotations.SerializedName
import java.util.UUID

data class SpraywallDTO(
    val id: UUID? = null,
    val name: String,
    val description: String,
    val photoUrl: String,
    @SerializedName("isPublic") val isPublic: Boolean,
    val gymId: UUID
)
