package com.example.sprayconnectapp.data.mappers

import com.example.sprayconnectapp.data.dto.Hold
import com.example.sprayconnectapp.data.model.HoldEntity

/**
 * Mapper: Wandelt einen [Hold] DTO in ein [HoldEntity] um.
 */

fun Hold.toEntity(boulderId: String) = HoldEntity(
    id = id,
    boulderId = boulderId,
    x = x,
    y = y,
    type = type
)

/**
 * Mapper: Wandelt einw [HoldEntity] aus der Datenbank in ein [Hold] DTO um.
 */

fun HoldEntity.toDto() = Hold(
    id = id,
    x = x,
    y = y,
    type = type
)
