package com.example.sprayconnectapp.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "holds",
    foreignKeys = [
        ForeignKey(
            entity = BoulderEntity::class,
            parentColumns = ["id"],
            childColumns  = ["boulderId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [Index("boulderId")]
)
data class HoldEntity(
    @PrimaryKey val id: String,
    val boulderId: String,
    val x: Float,
    val y: Float,
    val type: String
)