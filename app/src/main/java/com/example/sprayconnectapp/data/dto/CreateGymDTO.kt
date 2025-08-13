package com.example.sprayconnectapp.data.dto

import com.google.gson.annotations.SerializedName
import java.util.UUID

data class CreateGymDTO(
    val name: String,
    val location: String,
    val description: String,
    val createdBy: UUID,
    @SerializedName("isPublic") val isPublic: Boolean
)
