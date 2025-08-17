
package com.example.sprayconnectapp.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "spraywalls")
data class SpraywallEntity(
    @PrimaryKey val id: String,
    val gymId: String,
    val name: String,
    val description: String,
    val photoUrl: String,
    val isPublic: Boolean,
    val createdBy: String?
)
