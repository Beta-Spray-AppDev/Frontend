package com.example.sprayconnectapp.data.repository

import android.util.Log
import androidx.room.Transaction
import com.example.sprayconnectapp.data.local.GymDao
import com.example.sprayconnectapp.data.model.GymEntity
import com.example.sprayconnectapp.data.dto.Gym

class GymRepository(
    private val gymDao: GymDao
) {

    @Transaction
    suspend fun syncGymsFromBackend(gymDtos: List<Gym>, keepPinned: Boolean = true) {
        //  Upserts
        val remoteIds = gymDtos.map { it.id.toString() }.toSet()
        for (gymDto in gymDtos) {
            saveGymFromBackend(gymDto)
        }

        //  Pruning: alles löschen, was nicht mehr im Backend ist (außer ggf. pinned)
        val locals = gymDao.getAll()
        for (local in locals) {
            val notInBackend = local.id !in remoteIds
            val canDelete = !keepPinned || (keepPinned && !local.isPinned)
            if (notInBackend && canDelete) {
                gymDao.deleteById(local.id)
                Log.d("GymSync", "Lokal gelöscht (nicht mehr im Backend): ${local.name}")
            }
        }
    }

    // Speichert oder aktualisiert einen Gym aus dem Backend
    suspend fun saveGymFromBackend(gymDto: Gym, pinned: Boolean = false) {
        val existing = gymDao.getById(gymDto.id.toString())
        val keepPinned = existing?.isPinned ?: pinned

        val entity = GymEntity(
            id = gymDto.id.toString(),
            name = gymDto.name,
            location = gymDto.location,
            description = gymDto.description,
            createdBy = gymDto.createdBy.toString(),
            createdAt = gymDto.createdAt,
            lastUpdated = gymDto.lastUpdated,
            lastAccessed = System.currentTimeMillis(),
            isPinned = keepPinned
        )
        gymDao.insert(entity) // REPLACE
    }






    // Holt alle gespeicherten Gyms aus der lokalen DB
    suspend fun getAllGyms(): List<GymEntity> {
        return gymDao.getAll()
    }

    // Holt einen Gym nach ID
    suspend fun getGymById(id: String): GymEntity? {
        return gymDao.getById(id)
    }

    // Löscht Gyms, die alt + nicht gepinnt sind
    suspend fun deleteOldUnpinnedGyms() {
        val sevenDaysAgo = System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000
        gymDao.deleteStaleGyms(sevenDaysAgo)
    }

    // Löscht explizit einen Gym
    suspend fun deleteGymById(id: String) {
        gymDao.deleteById(id)
    }
}
