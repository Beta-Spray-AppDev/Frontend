
package com.example.sprayconnectapp.data.repository

import com.example.sprayconnectapp.data.dto.SpraywallDTO
import com.example.sprayconnectapp.data.local.BoulderDao
import com.example.sprayconnectapp.data.local.SpraywallDao
import com.example.sprayconnectapp.data.model.SpraywallEntity

class SpraywallRepository(
    private val spraywallDao: SpraywallDao,
    private val boulderDao: BoulderDao // falls vorhanden; sonst entfernen
) {
    /**
     * Spiegelt die vom Backend geholte Teilmenge (aktiv ODER archiviert) nach Room.
     * Es wird nur die entsprechende Teilmenge (isArchived = archived) vorher gepurged.
     */
    suspend fun syncFromBackend(gymId: String, remote: List<SpraywallDTO>, archived: Boolean) {
        // purge nur diese Teilmenge
        spraywallDao.purgeByGymAndArchived(gymId, archived)

        val entities = remote.map { dto ->
            SpraywallEntity(
                id = dto.id.toString(),
                gymId = (dto.gymId ?: java.util.UUID.fromString(gymId)).toString(),
                name = dto.name,
                description = dto.description,
                photoUrl = dto.photoUrl,
                isPublic = dto.isPublic,
                createdBy = dto.createdBy?.toString(),
                isArchived = dto.isArchived  // Backend-Quelle ist maßgeblich
            )
        }
        if (entities.isNotEmpty()) {
            spraywallDao.insertAll(entities)
        }
    }

    /** Lokal lesen – nach Gym und Archiv-Flag. */
    suspend fun getByGym(gymId: String, archived: Boolean): List<SpraywallEntity> =
        spraywallDao.getByGymAndArchived(gymId, archived)
}
