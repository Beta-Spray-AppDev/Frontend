package com.example.sprayconnectapp.data.dto

import java.util.UUID


data class BoulderDTO(
    val id: String? = null,
    val name: String = "",
    val difficulty: String = "",
    val spraywallId: String? = null,
    val holds: List<Hold> = emptyList(),

    val createdBy: String? = null,
    val createdAt: Long? = null,
    val lastUpdated: Long? = null,
    val createdByUsername: String? = null,
    val spraywallImageUrl: String? = null,
    val gymName: String? = null,
    val spraywallName: String? = null,
    val setterNote: String? = null,
    val avgStars: Double? = null,
    val starsCount: Int? = null,
    val totalSends: Long? = null,
    val avgGradeLabel: String? = null,
    val gradesCount: Int? = null

)