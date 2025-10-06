package com.example.sprayconnectapp.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "boulders")
data class BoulderEntity(
    @PrimaryKey val id: String,
    val spraywallId: String,
    val name: String,
    val difficulty: String,
    val holdsJson: String,
    val createdBy: String?,
    val createdAt: Long?,
    val lastUpdated: Long?,
    val setterNote: String? = null
    val avgStars: Double?,
    val starsCount: Int?
)