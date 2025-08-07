package com.example.sprayconnectapp.ui.screens.BoulderView

import android.content.Context
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sprayconnectapp.data.dto.CreateBoulderRequest
import com.example.sprayconnectapp.data.dto.Hold
import com.example.sprayconnectapp.data.dto.HoldType
import com.example.sprayconnectapp.network.RetrofitInstance
import kotlinx.coroutines.launch
import java.util.*


class CreateBoulderViewModel : ViewModel() {

    private val _uiState = mutableStateOf(
        CreateBoulderUiState(
            spraywallUrl = "https://spraywall-url.jpg/" // später dann dynamisch austauschen
        )
    )


    //aktueller Zsuatdn für Screen
    val uiState: State<CreateBoulderUiState> = _uiState

    //welche Farbe hat user gewählt
    fun selectHoldType(type: HoldType) {
        _uiState.value = _uiState.value.copy(selectedType = type)
    }


    //fügt Hold an der getappten Position hinzu
    fun addHoldNorm(nx: Float, ny: Float) {
        val newHold = Hold(
            id = UUID.randomUUID().toString(),
            x = nx,
            y = ny,
            type = _uiState.value.selectedType.name
        )
        _uiState.value = _uiState.value.copy(
            holds = _uiState.value.holds + newHold
        )
    }


    fun saveBoulder(context: Context, name: String, difficulty: String, spraywallId: String) {
        viewModelScope.launch {
            try {
                val request = CreateBoulderRequest(
                    name = name,
                    difficulty = difficulty,
                    spraywallId = spraywallId,
                    holds = _uiState.value.holds
                )
                Log.d("BoulderDebug", "Sending Boulder to backend: $request")


                val response = RetrofitInstance.getBoulderApi(context).createBoulder(request)

                if (response.isSuccessful) {
                    //  Erfolgsnachricht noch anzeigen
                } else {
                    //  Fehlerbehandlung
                }
            } catch (e: Exception) {
                //  Netzwerkfehler
            }
        }    }
}