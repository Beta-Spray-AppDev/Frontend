package com.example.sprayconnectapp.ui.screens.BoulderList

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sprayconnectapp.data.dto.BoulderDTO
import com.example.sprayconnectapp.network.RetrofitInstance
import kotlinx.coroutines.launch
import java.util.UUID
import androidx.compose.runtime.mutableStateOf

class BoulderListViewmodel : ViewModel(){


    var boulders = mutableStateOf<List<BoulderDTO>>(emptyList())
        private set

    var isLoading = mutableStateOf(false)
        private set

    var errorMessage = mutableStateOf<String?>(null)
        private set


    fun loadBoulders(context: Context, spraywallId: String) {
        viewModelScope.launch {
            isLoading.value = true
            errorMessage.value = null

            try {
                val uuid = UUID.fromString(spraywallId)
                val response = RetrofitInstance.getBoulderApi(context).getBouldersBySpraywall(uuid)

                if (response.isSuccessful) {
                    boulders.value = response.body() ?: emptyList()
                } else {
                    errorMessage.value = "Fehler: ${response.code()}"
                }

            } catch (e: Exception) {
                errorMessage.value = "Fehler: ${e.localizedMessage}"
            }

            isLoading.value = false
        }
    }

}