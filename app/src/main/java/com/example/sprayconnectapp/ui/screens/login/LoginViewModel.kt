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

    var isLoading by mutableStateOf(false)
        private set
    fun loginUser(context: Context) {

        // Input validieren
        if (!validateInputs()) return

        viewModelScope.launch {
            try {
                //Reset
                usernameError = null
                passwordError = null
                message = ""

                val request = LoginRequest(
                    username = username.trim(),
                    password = password // Für später wie handeln wir Leerzeichen?
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
                    if(response.code() == 400){
                        message = "Eingaben ungültig."
                    }
                //invalid credentials von Backend
                 else if (raw.contains("invalid_credentials", ignoreCase = true) ||
                        response.code() == 401) {
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



    fun clearMessage() {
        message = ""
    }


    private fun validateInputs(): Boolean {
        var ok = true
        if (username.isBlank()) {
            usernameError = "Bitte Benutzernamen eingeben."
            ok = false
        }
        if (password.isBlank()) {
            passwordError = "Bitte Passwort eingeben."
            ok = false
        }

        return ok
    }







}