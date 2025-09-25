package com.example.sprayconnectapp.ui.screens.home

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sprayconnectapp.data.dto.CreateGymDTO
import com.example.sprayconnectapp.data.dto.Gym
import com.example.sprayconnectapp.data.dto.feedback.CreateFeedbackDto
import com.example.sprayconnectapp.data.dto.feedback.FeedbackDto
import com.example.sprayconnectapp.data.local.AppDatabase
import com.example.sprayconnectapp.data.repository.FeedbackRepository
import com.example.sprayconnectapp.data.repository.GymRepository
import com.example.sprayconnectapp.network.RetrofitInstance
import com.example.sprayconnectapp.util.clearTokenFromPrefs
import kotlinx.coroutines.launch
import java.util.UUID


/**
 * Home-VM:
 * - lädt/synchronisiert die Gym-Liste (online/offline)
 * - erstellt neue Gyms
 * - verwaltet Logout
 */

class HomeViewModel : ViewModel() {

    private lateinit var gymRepository: GymRepository
    private lateinit var feedbackRepository: FeedbackRepository

    /** DAOs/Repository initialisieren */

    fun initRepository(context: Context) {
        val db = AppDatabase.getInstance(context)
        gymRepository = GymRepository(db.gymDao())
        feedbackRepository = FeedbackRepository(context.applicationContext)
    }



    var gyms = mutableStateOf<List<Gym>>(emptyList())
        private set

    var isLoading = mutableStateOf(false)
    var errorMessage = mutableStateOf<String?>(null)

    // ---- Feedback UI-State ----
    var feedbackSending = mutableStateOf(false)
        private set
    var feedbackError = mutableStateOf<String?>(null)
        private set
    var feedbackResult = mutableStateOf<FeedbackDto?>(null)
        private set

    /**
     * Lädt Gyms:
     * - Online: holt Serverliste, synchronisiert Room (upsert + prune), liest dann frisch aus Room
     * - Offline/Fehler: direkt aus Room lesen
     */

    fun loadGyms(context: Context) {
        viewModelScope.launch {
            isLoading.value = true
            errorMessage.value = null

            fun isOnline(context: Context): Boolean {
                val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                val network = cm.activeNetwork ?: return false
                val caps = cm.getNetworkCapabilities(network) ?: return false
                return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            }

            if (isOnline(context)) {
                try {
                    val response = RetrofitInstance.getGymApi(context).getAllGyms()
                    if (response.isSuccessful) {
                        val gymList = response.body() ?: emptyList()
                        gymRepository.syncGymsFromBackend(gymList, keepPinned = true)
                        val localGyms = gymRepository.getAllGyms()
                        gyms.value = localGyms.map { e ->
                            Gym(
                                id = UUID.fromString(e.id),
                                name = e.name,
                                location = e.location,
                                description = e.description,
                                createdBy = UUID.fromString(e.createdBy),
                                createdAt = e.createdAt,
                                lastUpdated = e.lastUpdated
                            )
                        }
                    } else {
                        errorMessage.value = "Fehler: ${response.code()}"
                        val localGyms = gymRepository.getAllGyms()
                        gyms.value = localGyms.map { e ->
                            Gym(
                                id = UUID.fromString(e.id),
                                name = e.name,
                                location = e.location,
                                description = e.description,
                                createdBy = UUID.fromString(e.createdBy),
                                createdAt = e.createdAt,
                                lastUpdated = e.lastUpdated
                            )
                        }
                    }
                } catch (e: Exception) {
                    errorMessage.value = "Netzwerkfehler: ${e.localizedMessage}"
                    val localGyms = gymRepository.getAllGyms()
                    gyms.value = localGyms.map { e ->
                        Gym(
                            id = UUID.fromString(e.id),
                            name = e.name,
                            location = e.location,
                            description = e.description,
                            createdBy = UUID.fromString(e.createdBy),
                            createdAt = e.createdAt,
                            lastUpdated = e.lastUpdated
                        )
                    }
                }
            } else {
                Log.d("GymSync", "OFFLINE – lade lokale Gyms")
                val localGyms = gymRepository.getAllGyms()
                gyms.value = localGyms.map { e ->
                    Gym(
                        id = UUID.fromString(e.id),
                        name = e.name,
                        location = e.location,
                        description = e.description,
                        createdBy = UUID.fromString(e.createdBy),
                        createdAt = e.createdAt,
                        lastUpdated = e.lastUpdated
                    )
                }
            }

            isLoading.value = false
        }
    }

    fun createGym(
        context: Context,
        dto: CreateGymDTO,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.getGymApi(context).createGym(dto)
                if (response.isSuccessful) onSuccess() else onError("Fehler: ${response.code()}")
            } catch (e: Exception) {
                onError("Fehler: ${e.localizedMessage}")
            }
        }
    }

    fun logout(context: Context) {
        clearTokenFromPrefs(context)
        RetrofitInstance.resetRetrofit()
    }

    // FEEDBACK: Senden
    fun sendFeedback(dto: CreateFeedbackDto) {
        viewModelScope.launch {
            feedbackSending.value = true
            feedbackError.value = null
            feedbackResult.value = null

            val result = feedbackRepository.sendFeedback(dto)
            result.onSuccess { fb ->
                feedbackResult.value = fb
            }.onFailure { t ->
                feedbackError.value = t.localizedMessage ?: "Unbekannter Fehler"
            }

            feedbackSending.value = false
        }
    }

    fun resetFeedbackState() {
        feedbackSending.value = false
        feedbackError.value = null
        feedbackResult.value = null
    }
}