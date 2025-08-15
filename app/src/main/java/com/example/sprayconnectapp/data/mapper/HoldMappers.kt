package com.example.sprayconnectapp.data.mappers

import com.example.sprayconnectapp.data.dto.Hold
import com.example.sprayconnectapp.data.model.HoldEntity

fun Hold.toEntity(boulderId: String) = HoldEntity(
    id = id,
    boulderId = boulderId,
    x = x,
    y = y,
    type = type
)

fun HoldEntity.toDto() = Hold(
    id = id,
    x = x,
    y = y,
    type = type
)
