package com.example.sprayconnectapp.ui.screens.register

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.example.sprayconnectapp.data.dto.RegisterRequest
import com.example.sprayconnectapp.network.RetrofitInstance
import kotlinx.coroutines.launch

class RegisterViewModel : ViewModel() {
    var email by mutableStateOf("")
        private set

    var password by mutableStateOf("")
        private set

    var username by mutableStateOf("")
        private set

    var message by mutableStateOf("") // Rückmeldung in UI



    fun onEmailChange(new: String) {
        email = new
    }

    fun onPasswordChange(new: String) {
        password = new
    }

    fun onUsernameChange(new: String) {
        username = new
    }

    fun registerUser(context: Context) {
        viewModelScope.launch {
            try {
                val request = RegisterRequest(
                    username = username,
                    email = email,
                    password = password
                )
                val response = RetrofitInstance.getApi(context).register(request)

                if (response.isSuccessful) {
                    message = "Registrierung erfolgreich!"
                } else {
                    message = "Registrierung fehlgeschlagen: ${response.code()}"
                }
            } catch (e: Exception) {
                message = "Netzwerkfehler: ${e.localizedMessage}"
                Log.e("RegisterViewModel", "Fehler bei Registrierung", e)
            }
        }
    }

}