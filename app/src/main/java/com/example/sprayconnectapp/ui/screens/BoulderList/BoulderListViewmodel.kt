// ui/screens/BoulderList/BoulderListViewModel.kt
package com.example.sprayconnectapp.ui.screens.BoulderList

import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sprayconnectapp.data.dto.BoulderDTO
import com.example.sprayconnectapp.data.dto.TickCreateRequest
import com.example.sprayconnectapp.data.local.AppDatabase
import com.example.sprayconnectapp.data.repository.BoulderRepository
import com.example.sprayconnectapp.network.RetrofitInstance
import com.example.sprayconnectapp.ui.screens.isOnline
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.UUID

private const val TAG_VM = "BoulderListVM"

class BoulderListViewModel : ViewModel() {
    private lateinit var repo: BoulderRepository

    /** Initialisiert das Repository (DB-DAOs injizieren) */
    fun initRepository(context: Context) {
        val db = AppDatabase.getInstance(context)
        repo = BoulderRepository(db.boulderDao(), db.holdDao())
        Log.d(TAG_VM, "Repository initialized")
    }

    // UI-State
    val boulders = mutableStateOf<List<BoulderDTO>>(emptyList())
    val isLoading = mutableStateOf(false)
    val errorMessage = mutableStateOf<String?>(null)

    val tickedBoulderIds = mutableStateOf<Set<String>>(emptySet())

    val tickStars = mutableStateOf<Map<String, Int>>(emptyMap())




    fun loadTickedBoulders(context: Context) = viewModelScope.launch {
        try {
            if (!isOnline(context)) {
                // Optional: Offline-Notiz
                Log.d(TAG_VM, "Offline – lade keine Ticks")
                tickedBoulderIds.value = emptySet()
                return@launch
            }

            val resp = RetrofitInstance.getBoulderApi(context).getMyTickedBoulders()
            Log.d(TAG_VM, "GET /boulders/ticks/mine code=${resp.code()} ok=${resp.isSuccessful}")

            if (resp.isSuccessful) {
                val list = resp.body().orEmpty()

                tickedBoulderIds.value = list.mapNotNull { it.boulder.id }.toSet()


                tickStars.value = list.mapNotNull { twb ->
                    val id = twb.boulder.id ?: return@mapNotNull null
                    val s  = twb.tick.stars ?: return@mapNotNull null
                    id to s
                }.toMap()
            }
             else {
                Log.w(TAG_VM, "Ticks laden fehlgeschlagen: ${resp.code()}")
            }
        } catch (t: Throwable) {
            Log.e(TAG_VM, "Fehler beim Laden der Ticks: ${t.message}", t)
        }
    }




    /**
     * Lädt Boulder zu einer Spraywall:
     * - Wenn online: vom Server ziehen und in lokale DB synchronisieren
     * - Immer: lokale DB lesen und ins UI bringen
     */

    fun load(context: Context, spraywallId: String) = viewModelScope.launch {
        Log.d(TAG_VM, "load(spraywallId=$spraywallId) start")
        isLoading.value = true
        errorMessage.value = null

        val online = isOnline(context)
        Log.d(TAG_VM, "online=$online")

        if (online) {
            try {
                val resp = RetrofitInstance.getBoulderApi(context)
                    .getBouldersBySpraywall(UUID.fromString(spraywallId))
                Log.d(TAG_VM, "GET /boulders/spraywall/$spraywallId code=${resp.code()} ok=${resp.isSuccessful}")

                if (resp.isSuccessful) {
                    val remote = resp.body().orEmpty()
                    Log.d(TAG_VM, "Remote size=${remote.size}")
                    // Serverstand → lokale DB (inkl. Holds)
                    repo.syncFromBackend(spraywallId, remote)
                } else {
                    errorMessage.value = "Fehler: ${resp.code()}"
                }
            } catch (t: Throwable) {
                Log.e(TAG_VM, "Network error: ${t.message}", t)
                errorMessage.value = "Netzwerkfehler: ${t.localizedMessage}"
            }
        } else {
            // Offline-Fall: Nutzer informieren, dann lokale DB zeigen
            errorMessage.value = "Offline – zeige lokale Daten"
        }


        // Lokale Daten immer ins UI übernehmen
        val local = repo.getLocalDtosBySpraywall(spraywallId)
        Log.d(TAG_VM, "Local to UI size=${local.size}")
        local.take(3).forEachIndexed { i, b ->
            Log.d(TAG_VM, "Local[$i]: id=${b.id} name=${b.name} holds=${b.holds.size}")
        }

        boulders.value = local
        isLoading.value = false
        Log.d(TAG_VM, "load() done")
    }




    fun deleteBoulder(
        context: Context,
        boulderId: String,
        onDone: (Boolean) -> Unit = {}
    ) = viewModelScope.launch {
        isLoading.value = true
        var ok = false
        try {
            val res = RetrofitInstance.getBoulderApi(context).deleteBoulder(boulderId)
            ok = res.isSuccessful
            if (ok) {
                // UI sofort aktualisieren: gelöschten Eintrag lokal entfernen
                boulders.value = boulders.value.filterNot { it.id == boulderId }
            } else {
                errorMessage.value = "Löschen fehlgeschlagen (${res.code()})"
            }
        } catch (t: Throwable) {
            errorMessage.value = t.message
        } finally {
            isLoading.value = false
            onDone(ok)
        }
    }







}
