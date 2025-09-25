package com.example.sprayconnectapp.ui.screens.spraywall

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sprayconnectapp.data.dto.SpraywallDTO
import com.example.sprayconnectapp.data.local.AppDatabase
import com.example.sprayconnectapp.data.model.SpraywallEntity
import com.example.sprayconnectapp.data.repository.SpraywallRepository
import com.example.sprayconnectapp.network.RetrofitInstance
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * ViewModel für Spraywalls:
 * - Synchronisiert Liste pro Gym zwischen Backend und Room
 * - Erstellen einer Spraywall (nach Bild-Upload)
 * - Offline-Fallback: lokale Daten anzeigen
 */

class SpraywallViewModel(context: Context) : ViewModel() {

    private val repo: SpraywallRepository

    init {
        val db = AppDatabase.getInstance(context.applicationContext)
        repo = SpraywallRepository(db.spraywallDao(), db.boulderDao())
    }

    var spraywalls = mutableStateOf<List<SpraywallDTO>>(emptyList())
        private set
    var isLoading = mutableStateOf(false)
    var errorMessage = mutableStateOf<String?>(null)


    /**
     * Lädt alle Spraywalls eines Gyms:
     * - Falls online: holt vom Backend und spiegelt in Room (purge + upsert)
     * - Danach immer lokal aus Room lesen → UI State
     */

    fun loadSpraywalls(context: Context, gymId: String) {
        viewModelScope.launch {
            isLoading.value = true
            errorMessage.value = null

            val online = isOnline(context)
            if (online) {
                try {
                    val response = RetrofitInstance.getSpraywallApi(context)
                        .getSpraywallsByGym(UUID.fromString(gymId))

                    if (response.isSuccessful) {
                        val remoteList = response.body().orEmpty()
                        repo.syncFromBackend(gymId, remoteList)
                    } else {
                        errorMessage.value = "Fehler: ${response.code()}"
                    }
                } catch (e: Exception) {
                    errorMessage.value = "Netzwerkfehler: ${e.localizedMessage}"
                }
            } else {
                if (spraywalls.value.isEmpty()) {
                    errorMessage.value = "Offline – zeige lokale Daten"
                }
            }

            val locals = repo.getByGym(gymId)
            spraywalls.value = locals.map { it.toDto() }

            isLoading.value = false
        }
    }

    /**
     * Legt eine Spraywall am Backend an (benötigt Internet).
     * - Nach Erfolg wird die gesamte Liste neu synchronisiert, damit die UI konsistent ist.
     */

    fun createSpraywall(
        context: Context,
        dto: SpraywallDTO,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            if (!isOnline(context)) {
                onError("Du bist offline – Erstellen nur mit Internet möglich.")
                return@launch
            }
            isLoading.value = true
            errorMessage.value = null
            try {
                val response = RetrofitInstance.getSpraywallApi(context).createSpraywall(dto)
                if (response.isSuccessful) {
                    val gymId = dto.gymId?.toString() ?: return@launch run {
                        onSuccess(); isLoading.value = false
                    }
                    val listResp = RetrofitInstance.getSpraywallApi(context)
                        .getSpraywallsByGym(UUID.fromString(gymId))
                    if (listResp.isSuccessful) {
                        repo.syncFromBackend(gymId, listResp.body().orEmpty())
                    }
                    onSuccess()
                } else {
                    onError("Fehler: ${response.code()}")
                }
            } catch (e: Exception) {
                onError("Fehler: ${e.localizedMessage}")
            } finally {
                isLoading.value = false
            }
        }
    }

    /** Simple Online-Check über ConnectivityManager. */
    private fun isOnline(ctx: Context): Boolean {
        val cm = ctx.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val net = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(net) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    /** Mapper Room → DTO (nur relevante Felder). */

    private fun SpraywallEntity.toDto(): SpraywallDTO =
        SpraywallDTO(
            id = UUID.fromString(id),
            name = name,
            description = description,
            photoUrl = photoUrl,
            isPublic = isPublic,
            gymId = UUID.fromString(gymId),
            createdBy = createdBy?.let { UUID.fromString(it) }
        )
}

