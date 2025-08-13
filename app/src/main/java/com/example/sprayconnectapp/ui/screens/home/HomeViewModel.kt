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
import com.example.sprayconnectapp.network.RetrofitInstance
import com.example.sprayconnectapp.util.clearTokenFromPrefs
import kotlinx.coroutines.launch
import com.example.sprayconnectapp.data.repository.GymRepository
import com.example.sprayconnectapp.data.local.AppDatabase
import java.util.UUID

class HomeViewModel : ViewModel() {

    private lateinit var gymRepository: GymRepository


    fun initRepository(context: Context) {
        val db = AppDatabase.getInstance(context)
        gymRepository = GymRepository(db.gymDao())
    }



    var gyms = mutableStateOf<List<Gym>>(emptyList())
        private set

    var isLoading = mutableStateOf(false)
    var errorMessage = mutableStateOf<String?>(null)


    fun loadGyms(context: Context) {
        viewModelScope.launch {
            isLoading.value = true
            errorMessage.value = null

            fun isOnline(context: Context): Boolean {
                val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                val network = cm.activeNetwork ?: return false
                val capabilities = cm.getNetworkCapabilities(network) ?: return false
                return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            }


            if (isOnline(context)) {

                try {
                    val response = RetrofitInstance.getGymApi(context).getAllGyms()
                    if (response.isSuccessful) {
                        val gymList = response.body() ?: emptyList()

                        // EINZIGER CALL: upsert + prune
                        gymRepository.syncGymsFromBackend(gymList, keepPinned = true)

                        // frisch aus Room lesen
                        val localGyms = gymRepository.getAllGyms()
                        gyms.value = localGyms.map { entity ->
                            Gym(
                                id = UUID.fromString(entity.id),
                                name = entity.name,
                                location = entity.location,
                                description = entity.description,
                                createdBy = UUID.fromString(entity.createdBy),
                                createdAt = entity.createdAt,
                                lastUpdated = entity.lastUpdated
                            )
                        }
                    } else {
                        errorMessage.value = "Fehler: ${response.code()}"
                        val localGyms = gymRepository.getAllGyms()
                        gyms.value = localGyms.map { entity ->
                            Gym(
                                id = UUID.fromString(entity.id),
                                name = entity.name,
                                location = entity.location,
                                description = entity.description,
                                createdBy = UUID.fromString(entity.createdBy),
                                createdAt = entity.createdAt,
                                lastUpdated = entity.lastUpdated
                            )
                        }

                    }
                } catch (e: Exception) {
                    errorMessage.value = "Netzwerkfehler: ${e.localizedMessage}"
                    val localGyms = gymRepository.getAllGyms()
                    gyms.value = localGyms.map { entity ->
                        Gym(
                            id = UUID.fromString(entity.id),
                            name = entity.name,
                            location = entity.location,
                            description = entity.description,
                            createdBy = UUID.fromString(entity.createdBy),
                            createdAt = entity.createdAt,
                            lastUpdated = entity.lastUpdated
                        )
                    }

                }
            } else {
                Log.d("GymSync", "OFFLINE – lade lokale Gyms")
                val localGyms = gymRepository.getAllGyms()
                gyms.value = localGyms.map { entity ->
                    Gym(
                        id = UUID.fromString(entity.id),
                        name = entity.name,
                        location = entity.location,
                        description = entity.description,
                        createdBy = UUID.fromString(entity.createdBy),
                        createdAt = entity.createdAt,
                        lastUpdated = entity.lastUpdated
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
                if (response.isSuccessful) {
                    onSuccess()
                } else {
                    onError("Fehler: ${response.code()}")
                }
            } catch (e: Exception) {
                onError("Fehler: ${e.localizedMessage}")
            }
        }
    }




    fun logout(context: Context) {
        clearTokenFromPrefs(context)
        RetrofitInstance.resetRetrofit()

    }

}