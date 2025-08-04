package com.example.sprayconnectapp.data.repository

import android.util.Log
import com.example.sprayconnectapp.data.local.GymDao
import com.example.sprayconnectapp.data.model.GymEntity
import com.example.sprayconnectapp.data.dto.Gym

class GymRepository(
    private val gymDao: GymDao
) {

    // Speichert oder aktualisiert einen Gym aus dem Backend
    suspend fun saveGymFromBackend(gymDto: Gym, pinned: Boolean = false) {
        val local = gymDao.getById(gymDto.id.toString())

        // Wenn lokal noch nicht vorhanden → neu speichern
        if (local == null) {
            val newGym = GymEntity(
                id = gymDto.id.toString(),
                name = gymDto.name,
                location = gymDto.location,
                description = gymDto.description,
                createdBy = gymDto.createdBy.toString(),
                createdAt = gymDto.createdAt,
                lastUpdated = gymDto.lastUpdated,
                lastAccessed = System.currentTimeMillis(),
                isPinned = pinned
            )
            gymDao.insert(newGym)
            Log.d("GymSync", "Neuer Gym gespeichert: ${newGym.name}")
            return
        }

        // Wenn Backend-Daten aktueller sind → aktualisieren
        if (gymDto.lastUpdated > local.lastUpdated) {
            val updatedGym = local.copy(
                name = gymDto.name,
                location = gymDto.location,
                description = gymDto.description,
                createdBy = gymDto.createdBy.toString(),
                createdAt = gymDto.createdAt,
                lastUpdated = gymDto.lastUpdated,
                lastAccessed = System.currentTimeMillis()
            )
            gymDao.insert(updatedGym)
            Log.d("GymSync", "Gym aktualisiert: ${updatedGym.name}")
        } else {
            // Backend-Daten sind älter oder identisch → nichts tun
            Log.d("GymSync", "Gym ist bereits aktuell: ${local.name}")
        }
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
