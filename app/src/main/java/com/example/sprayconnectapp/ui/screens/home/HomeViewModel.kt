package com.example.sprayconnectapp.ui.screens.home

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sprayconnectapp.data.dto.Gym
import com.example.sprayconnectapp.network.RetrofitInstance
import com.example.sprayconnectapp.util.clearTokenFromPrefs
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {


    var gyms = mutableStateOf<List<Gym>>(emptyList())
        private set

    var isLoading = mutableStateOf(false)
    var errorMessage = mutableStateOf<String?>(null)


    fun loadGyms(context: Context) {
        viewModelScope.launch {
            isLoading.value = true
            errorMessage.value = null
            try {
                val response = RetrofitInstance.getGymApi(context).getAllGyms()
                if (response.isSuccessful) {
                    gyms.value = response.body() ?: emptyList()
                } else {
                    errorMessage.value = "Fehler: ${response.code()}"
                }
            } catch (e: Exception) {
                errorMessage.value = "Netzwerkfehler: ${e.localizedMessage}"
            }
            isLoading.value = false
        }
    }


    fun logout(context: Context) {
        clearTokenFromPrefs(context)
    }

}