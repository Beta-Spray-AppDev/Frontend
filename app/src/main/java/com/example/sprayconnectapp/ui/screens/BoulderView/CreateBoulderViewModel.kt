package com.example.sprayconnectapp.ui.screens.BoulderView

import android.content.Context
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sprayconnectapp.data.dto.BoulderDTO
import com.example.sprayconnectapp.data.dto.CreateBoulderRequest
import com.example.sprayconnectapp.data.dto.Hold
import com.example.sprayconnectapp.data.dto.HoldType
import com.example.sprayconnectapp.network.RetrofitInstance
import kotlinx.coroutines.launch
import java.util.*


class CreateBoulderViewModel : ViewModel() {

    private val _uiState = mutableStateOf(CreateBoulderUiState())


    //aktueller Zsuatdn für Screen
    val uiState: State<CreateBoulderUiState> = _uiState

    private val _boulders = mutableStateOf<List<BoulderDTO>>(emptyList())
    val boulders: State<List<BoulderDTO>> = _boulders

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _errorMessage = mutableStateOf<String?>(null)
    val errorMessage: State<String?> = _errorMessage

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
        }
    }

    fun updateHoldPosition(id: String, newX: Float, newY: Float) {
        _uiState.value = _uiState.value.copy(
            holds = _uiState.value.holds.map {
                if (it.id == id) it.copy(x = newX, y = newY) else it
            }
        )
    }


    fun selectHold(id: String) {
        _uiState.value = _uiState.value.copy(selectedHoldId = id)
    }

    fun loadBoulder(context: Context, boulderId: String) {
        viewModelScope.launch {
            try {
                val res = RetrofitInstance.getBoulderApi(context)
                    .getBoulderById(UUID.fromString(boulderId))
                if (res.isSuccessful) {
                    val boulder = res.body()
                    if (boulder != null) {
                        _uiState.value = _uiState.value.copy(boulder = boulder)
                    }
                } else {
                    Log.e("Boulder", "Fehler: ${res.code()}")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

}