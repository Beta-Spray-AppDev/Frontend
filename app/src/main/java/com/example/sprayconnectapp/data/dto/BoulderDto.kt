package com.example.sprayconnectapp.data.dto

import java.util.UUID

data class BoulderDTO(
    val id: UUID,
    val name: String,
    val difficulty: String,
    val spraywallId: UUID
)