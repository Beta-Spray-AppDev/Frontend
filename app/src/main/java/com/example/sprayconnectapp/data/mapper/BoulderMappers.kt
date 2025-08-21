package com.example.sprayconnectapp.data.mappers

import com.example.sprayconnectapp.data.dto.BoulderDTO
import com.example.sprayconnectapp.data.dto.Hold
import com.example.sprayconnectapp.data.model.BoulderEntity
import com.example.sprayconnectapp.data.model.HoldEntity

// Entity -> DTO (ohne Holds)
fun BoulderEntity.toDtoBase() = BoulderDTO(
    id = id,
    spraywallId = spraywallId,
    name = name,
    difficulty = difficulty,
    holds = emptyList(),
    createdBy = createdBy,
    createdAt = createdAt,
    lastUpdated = lastUpdated
)

// Entity -> DTO
fun BoulderEntity.toDtoWith(holds: List<Hold>) = BoulderDTO(
    id = id,
    spraywallId = spraywallId,
    name = name,
    difficulty = difficulty,
    holds = holds,
    createdBy = createdBy,
    createdAt = createdAt,
    lastUpdated = lastUpdated
)

// DTO -> Entity
fun BoulderDTO.toEntity() = BoulderEntity(
    id = requireNotNull(id),
    spraywallId = requireNotNull(spraywallId),
    name = name,
    difficulty = difficulty,
    holdsJson = "[]",
    createdBy = createdBy,
    createdAt = createdAt,
    lastUpdated = lastUpdated
)


