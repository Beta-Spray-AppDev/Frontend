// ui/screens/BoulderList/BoulderListViewModel.kt
package com.example.sprayconnectapp.ui.screens.BoulderList

import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sprayconnectapp.data.dto.BoulderDTO
import com.example.sprayconnectapp.data.local.AppDatabase
import com.example.sprayconnectapp.data.repository.BoulderRepository
import com.example.sprayconnectapp.network.RetrofitInstance
import com.example.sprayconnectapp.ui.screens.isOnline
import kotlinx.coroutines.launch
import java.util.UUID

private const val TAG_VM = "BoulderListVM"

class BoulderListViewModel : ViewModel() {
    private lateinit var repo: BoulderRepository

    fun initRepository(context: Context) {
        val db = AppDatabase.getInstance(context)
        repo = BoulderRepository(db.boulderDao(), db.holdDao())
        Log.d(TAG_VM, "Repository initialized")
    }

    val boulders = mutableStateOf<List<BoulderDTO>>(emptyList())
    val isLoading = mutableStateOf(false)
    val errorMessage = mutableStateOf<String?>(null)

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
                    repo.syncFromBackend(spraywallId, remote)
                } else {
                    errorMessage.value = "Fehler: ${resp.code()}"
                }
            } catch (t: Throwable) {
                Log.e(TAG_VM, "Network error: ${t.message}", t)
                errorMessage.value = "Netzwerkfehler: ${t.localizedMessage}"
            }
        } else {
            errorMessage.value = "Offline – zeige lokale Daten"
        }


        val local = repo.getLocalDtosBySpraywall(spraywallId)
        Log.d(TAG_VM, "Local to UI size=${local.size}")
        local.take(3).forEachIndexed { i, b ->
            Log.d(TAG_VM, "Local[$i]: id=${b.id} name=${b.name} holds=${b.holds.size}")
        }

        boulders.value = local
        isLoading.value = false
        Log.d(TAG_VM, "load() done")
    }
}
