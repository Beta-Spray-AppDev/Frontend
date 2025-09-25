package com.example.sprayconnectapp.data.repository

import android.util.Log
import com.example.sprayconnectapp.data.dto.BoulderDTO
import com.example.sprayconnectapp.data.dto.CreateBoulderRequest
import com.example.sprayconnectapp.data.local.BoulderDao
import com.example.sprayconnectapp.data.local.HoldDao
import com.example.sprayconnectapp.data.mappers.*
import com.example.sprayconnectapp.data.model.BoulderEntity
import com.example.sprayconnectapp.network.BoulderApi

private const val TAG_REPO = "BoulderRepo"

/**
 * Repository für Boulder.
 *
 * Kapselt lokale Datenbankzugriffe (Room) und Remote-Operationen (API).
 * Stellt Methoden zum Lesen, Synchronisieren und Schreiben bereit.
 */


class BoulderRepository(
    private val boulderDao: BoulderDao,
    private val holdDao: HoldDao
) {
    //Local reads
    /** Alle Boulder einer Spraywall lokal abrufen */

    suspend fun getLocalBySpraywall(spraywallId: String): List<BoulderEntity> =
        boulderDao.getBySpraywall(spraywallId)


    /** Einzelnen Boulder + zugehörige Holds als DTO laden */
    suspend fun getLocalBoulderWithHolds(id: String): BoulderDTO? {
        val be = boulderDao.getById(id) ?: return null
        val holds = holdDao.getByBoulder(id).map { it.toDto() }
        return be.toDtoWith(holds)
    }


    /** Alle Boulder einer Spraywall inkl. Holds als DTO-Liste laden */
    suspend fun getLocalDtosBySpraywall(spraywallId: String): List<BoulderDTO> {
        val es = boulderDao.getBySpraywall(spraywallId)
        return es.map { e ->
            val holds = holdDao.getByBoulder(e.id).map { it.toDto() }
            e.toDtoWith(holds)
        }
    }

    // Sync (list)

    /**
     * Synchronisiert lokale Boulder und Holds mit einer Liste vom Server.
     * - Löscht lokale Boulder, die es am Server nicht mehr gibt
     * - Upsert für neue/geänderte Boulder
     * - Ersetzt Holds komplett durch Serverstand
     */

    suspend fun syncFromBackend(spraywallId: String, remote: List<BoulderDTO>) {
        Log.d(TAG_REPO, "syncFromBackend spraywall=$spraywallId remote=${remote.size}")

        val remoteById = remote.filter { it.id != null }.associateBy { it.id!! }
        val remoteIds = remoteById.keys

        val locals = boulderDao.getBySpraywall(spraywallId)
        val localById = locals.associateBy { it.id }

        //  purge: Boulder, die es am Server nicht mehr gibt
        for (l in locals) {
            if (l.id !in remoteIds) {
                boulderDao.deleteById(l.id)
                holdDao.deleteByBoulder(l.id)
            }
        }

        //  upsert: erst die PARENTS speichern
        val toUpsert = mutableListOf<BoulderEntity>()
        for ((id, dto) in remoteById) {
            val rLU = dto.lastUpdated ?: Long.MIN_VALUE
            val lLU = localById[id]?.lastUpdated ?: Long.MIN_VALUE
            if (localById[id] == null || rLU > lLU) {
                toUpsert += dto.toEntity()       // <- Mapper-Funktion
            }
        }
        if (toUpsert.isNotEmpty()) {
            boulderDao.insertAll(toUpsert)
        }


        var totalHolds = 0
        for ((id, dto) in remoteById) {
            holdDao.deleteByBoulder(id)
            val holdEntities = dto.holds.map { it.toEntity(id) } // HoldDTO -> HoldEntity(boulderId=id)
            if (holdEntities.isNotEmpty()) {
                holdDao.insertAll(holdEntities)
                totalHolds += holdEntities.size
            }
        }

        Log.d(TAG_REPO, "sync done upsert=${toUpsert.size} holds=$totalHolds")
    }

    /** Fügt einen Boulder aus dem Server in DB ein oder aktualisiert ihn */
    suspend fun upsertFromRemote(dto: BoulderDTO) {
        val id = requireNotNull(dto.id)
        boulderDao.upsert(dto.toEntity())
        holdDao.deleteByBoulder(id)
        holdDao.insertAll(dto.holds.map { it.toEntity(id) })
    }


    /** Erstellt einen Boulder am Server und speichert ihn lokal */
    suspend fun createOnline(api: BoulderApi, req: CreateBoulderRequest): Result<BoulderDTO> =
        runCatching {
            val resp = api.createBoulder(req)
            if (!resp.isSuccessful) error("Create failed ${resp.code()}")
            val dto = requireNotNull(resp.body())
            upsertFromRemote(dto)
            dto
        }

    /** Aktualisiert einen Boulder am Server und lokal */
    suspend fun updateOnline(api: BoulderApi, id: String, dto: BoulderDTO): Result<BoulderDTO> =
        runCatching {
            val resp = api.updateBoulder(id, dto)
            if (!resp.isSuccessful) error("Update failed ${resp.code()}")
            val body = requireNotNull(resp.body())
            upsertFromRemote(body)
            body
        }

    suspend fun deleteOnline(api: BoulderApi, id: String): Result<Unit> =
        runCatching {
            val resp = api.deleteBoulder(id)
            if (!resp.isSuccessful) error("Delete failed ${resp.code()}")
            holdDao.deleteByBoulder(id)
            boulderDao.deleteById(id)
        }
}
