package com.example.sprayconnectapp.data.dto

import com.google.gson.annotations.SerializedName
import java.util.UUID

data class SpraywallDTO(
    val id: UUID? = null,
    val name: String,
    val description: String,
    val photoUrl: String,

    // Backend: "publicVisible"
    @SerializedName("publicVisible")
    val isPublic: Boolean,

    val gymId: UUID,
    val createdBy: UUID? = null,

    // Backend: "archived"
    @SerializedName("archived")
    val isArchived: Boolean = false
)

