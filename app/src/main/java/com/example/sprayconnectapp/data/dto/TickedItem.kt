package com.example.sprayconnectapp.data.dto

data class TickedItem (
    val tickId: String,
    val boulderId: String?,                 // kann null sein (gelöschter Boulder)
    val name: String,                       // kommt bevorzugt aus dem Tick-Snapshot
    val displayedDifficulty: String?,
    val spraywallId: String?,
    val spraywallImageUrl: String?
)