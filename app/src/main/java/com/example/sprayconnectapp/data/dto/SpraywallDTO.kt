package com.example.sprayconnectapp.data.dto

import java.util.UUID

data class SpraywallDTO(
    val name: String,
    val description: String,
    val photoUrl: String,
    val gymId: UUID,
    val isPublic: Boolean
)
