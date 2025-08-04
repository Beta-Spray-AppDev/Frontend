package com.example.sprayconnectapp.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "gyms")
data class GymEntity(
    @PrimaryKey val id: String,
    val name: String,
    val location: String,
    val description: String,
    val createdBy: String,
    val createdAt: Long,
    val lastUpdated: Long,
    val lastAccessed: Long,
    val isPinned: Boolean
)