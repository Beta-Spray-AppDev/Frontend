package com.example.sprayconnectapp.data.dto

data class TickDto(
    val id: String? = null,
    val boulderId: String,
    val userId: String? = null,
    val createdAt: Long? = null,
    val stars: Int? = null,
    val proposedGrade: String? = null
)

data class TickCreateRequest(
    val stars: Int? = null,           // 1..5
    val proposedGrade: String? = null // z.B. "6B+"
)

data class TickWithBoulderDto(
    val tick: TickDto,
    val boulder: BoulderDTO
)
