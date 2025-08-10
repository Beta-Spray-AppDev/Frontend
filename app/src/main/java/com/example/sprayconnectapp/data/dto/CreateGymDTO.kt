package com.example.sprayconnectapp.data.dto

import java.util.UUID

data class CreateGymDTO(
    val name: String,
    val location: String,
    val description: String,
    val createdBy: UUID
)
