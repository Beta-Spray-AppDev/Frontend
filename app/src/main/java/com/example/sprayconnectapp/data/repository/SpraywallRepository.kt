// data/repository/SpraywallRepository.kt
package com.example.sprayconnectapp.data.repository

import android.util.Log
import com.example.sprayconnectapp.data.local.SpraywallDao
import com.example.sprayconnectapp.data.local.BoulderDao
import com.example.sprayconnectapp.data.model.SpraywallEntity
import com.example.sprayconnectapp.data.dto.SpraywallDTO
import java.util.UUID

class SpraywallRepository(
    private val spraywallDao: SpraywallDao,
    private val boulderDao: BoulderDao
) {
    suspend fun getByGym(gymId: String): List<SpraywallEntity> =
        spraywallDao.getByGym(gymId)

    suspend fun syncFromBackend(gymId: String, remote: List<SpraywallDTO>) {
        val remoteIds = remote.mapNotNull { it.id?.toString() }.toSet()

        // alte lokale, die es am Server nicht mehr gibt, löschen (+ abhängige Boulder)
        val locals = spraywallDao.getByGym(gymId)
        for (l in locals) {
            if (l.id !in remoteIds) {
                spraywallDao.deleteById(l.id)

            }
        }

        // upsert aller Server-Einträge
        val entities = remote
            .filter { it.id != null }
            .map { it.toEntity() }

        spraywallDao.insertAll(entities)
    }
}

private fun SpraywallDTO.toEntity() = SpraywallEntity(
    id          = requireNotNull(id).toString(),
    gymId       = gymId.toString(),
    name        = name,
    description = description,
    photoUrl    = photoUrl,
    isPublic    = isPublic,
    createdBy   = createdBy?.toString()
)

