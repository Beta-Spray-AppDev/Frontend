package com.example.sprayconnectapp.ui.screens.spraywall

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sprayconnectapp.data.dto.SpraywallDTO
import com.example.sprayconnectapp.network.RetrofitInstance
import kotlinx.coroutines.launch
import java.util.UUID

class SpraywallViewModel : ViewModel() {

    var spraywalls = mutableStateOf<List<SpraywallDTO>>(emptyList())
        private set

    var isLoading = mutableStateOf(false)
    var errorMessage = mutableStateOf<String?>(null)

    fun loadSpraywalls(context: Context, gymId: String) {
        viewModelScope.launch {
            isLoading.value = true
            errorMessage.value = null

            fun isOnline(ctx: Context): Boolean {
                val cm = ctx.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                val network = cm.activeNetwork ?: return false
                val capabilities = cm.getNetworkCapabilities(network) ?: return false
                return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            }

            if (isOnline(context)) {
                try {
                    val uuid = UUID.fromString(gymId)
                    val response = RetrofitInstance.getSpraywallApi(context).getSpraywallsByGym(uuid)

                    if (response.isSuccessful) {
                        spraywalls.value = response.body() ?: emptyList()
                    } else {
                        errorMessage.value = "Fehler: ${response.code()}"
                        spraywalls.value = emptyList()
                    }

                } catch (e: Exception) {
                    errorMessage.value = "Netzwerkfehler: ${e.localizedMessage}"
                    spraywalls.value = emptyList()
                }
            } else {
                errorMessage.value = "Keine Internetverbindung"
                spraywalls.value = emptyList()
            }

            isLoading.value = false
        }
    }

    fun createSpraywall(context: Context, dto: SpraywallDTO, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            isLoading.value = true
            errorMessage.value = null

            try {
                val response = RetrofitInstance.getSpraywallApi(context).createSpraywall(dto)
                if (response.isSuccessful) {
                    onSuccess()
                } else {
                    onError("Fehler: ${response.code()}")
                }
            } catch (e: Exception) {
                onError("Fehler: ${e.localizedMessage}")
            }

            isLoading.value = false
        }
    }

}
