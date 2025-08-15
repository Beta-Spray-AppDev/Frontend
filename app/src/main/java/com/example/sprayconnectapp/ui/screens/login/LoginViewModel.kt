package com.example.sprayconnectapp.ui.screens.login

import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.example.sprayconnectapp.data.dto.LoginRequest
import com.example.sprayconnectapp.network.RetrofitInstance
import kotlinx.coroutines.launch

import com.example.sprayconnectapp.util.saveTokenToPrefs



class LoginViewModel : ViewModel() {

    var username by mutableStateOf("")
        private set

    var password by mutableStateOf("")
        private set

    var message by mutableStateOf("")

    var usernameError by mutableStateOf<String?>(null)
        private set

    var passwordError by mutableStateOf<String?>(null)
        private set


    fun onUsernameChange(new: String) {
        username = new
        usernameError = null
        message = ""
    }

    fun onPasswordChange(new: String) {
        password = new
        passwordError = null
        message = ""
    }
    fun loginUser(context: Context) {
        viewModelScope.launch {
            try {
                //Reset
                usernameError = null
                passwordError = null
                message = ""

                val request = LoginRequest(
                    username = username,
                    password = password
                )

                val response = RetrofitInstance.getApi(context).login(request)

                if (response.isSuccessful) {
                    val token = response.body()

                    if (!token.isNullOrBlank() && token.startsWith("ey")) {
                        saveTokenToPrefs(context, token)
                        message = "Login erfolgreich"

                    } else {
                        message = "Login fehlgeschlagen: Ungültige Antwort"
                    }
                } else {
                    val raw = response.errorBody()?.string()?.trim().orEmpty()
                //invalid credentials von Backend
                    if (raw.contains("invalid_credentials")) {
                        // Benutzername ODER Passwort ist falsch
                        passwordError = "Benutzername oder Passwort ist falsch."
                        message = ""
                    } else {
                        message = "Login fehlgeschlagen (${response.code()})"
                    }                }
            } catch (e: Exception) {
                message = "Netzwerkfehler: ${e.localizedMessage}"
                Log.e("LoginViewModel", "Fehler beim Login", e)
            }
        }
    }










}