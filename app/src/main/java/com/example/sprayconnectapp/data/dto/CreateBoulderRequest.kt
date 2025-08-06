package com.example.sprayconnectapp.data.dto

data class CreateBoulderRequest(
    val name: String,
    val difficulty: String,
    val spraywallId: String,
    val holds: List<Hold>
)
