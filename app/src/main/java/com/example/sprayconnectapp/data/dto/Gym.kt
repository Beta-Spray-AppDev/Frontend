package com.example.sprayconnectapp.data.dto

import java.util.UUID

data class Gym(
    val id: UUID,
    val name: String,
    val location: String,
    val description: String,
    val createdBy: UUID,
    val createdAt: Long,
    val lastUpdated: Long
)