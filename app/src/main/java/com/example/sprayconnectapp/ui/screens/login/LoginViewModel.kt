package com.example.sprayconnectapp.ui.screens.login

import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.example.sprayconnectapp.data.dto.LoginRequest
import com.example.sprayconnectapp.data.dto.TokenResponse
import com.example.sprayconnectapp.network.RetrofitInstance
import com.example.sprayconnectapp.util.TokenStore
import kotlinx.coroutines.launch



/**
 * ViewModel für den Login-Prozess.
 * Verantwortlichkeiten:
 * - Hält UI-States (Inputs, Loading, Fehler, Message)
 * - Clientseitige Validierung
 * - API-Call /auth/login
 * - Token speichern + Erfolgsmeldung (triggert Navigation)
 */

class LoginViewModel : ViewModel() {


    // Ui States für Textfelder
    var username by mutableStateOf("")
        private set

    var password by mutableStateOf("")
        private set


    // message für Toast plus navigation Trigger
    var message by mutableStateOf("")


    // Für rote error Messages unter den Inputs
    var usernameError by mutableStateOf<String?>(null)
        private set

    var passwordError by mutableStateOf<String?>(null)
        private set



    // Input-Handler
    fun onUsernameChange(new: String) {
        username = new
        usernameError = null
        message = ""
    }

    fun onPasswordChange(new: String) {
        password = new
        passwordError = null // Fehler zurücksetzen wenn User tippt
        message = "" // alten Meldungen löschen
    }


    // Ladespinner - Steuerung
    var isLoading by mutableStateOf(false)
        private set


    /**
     * Führt den Login aus:
     * - Validiert Inputs
     * - Baut Request und ruft Retrofit-API
     * - Bei Erfolg: Token speichern + Message setzen
     * - Bei Fehler: differenzierte Fehlermeldungen
     */

    fun loginUser(context: Context) {

        if (isLoading) return
        // Input Client-Seitig validieren
        if (!validateInputs()) return

        viewModelScope.launch {
            try {
                isLoading = true // Spinner an

                //Reset
                usernameError = null
                passwordError = null
                message = ""


                // Request Dto bauen
                val request = LoginRequest(
                    username = username.trim(),
                    password = password
                )

                // Api Call via Retrofit
                val response = RetrofitInstance.getApi(context).login(request)

                if (response.isSuccessful) {
                    val tokens: TokenResponse? = response.body()

                    if (tokens != null) {
                        // NEU: sicher speichern (access + refresh)
                        TokenStore(context).save(tokens.accessToken, tokens.refreshToken)
                        message = "Login erfolgreich"

                    } else {
                        message = "Login fehlgeschlagen: Ungültige Antwort"
                    }
                } else {
                    // Fehlerbody von Backend
                    val raw = response.errorBody()?.string()?.trim().orEmpty()

                    // Serverseitige Validierung Fehlgeschlagen
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
            finally {
                isLoading = false // wird garantiert ausgeführt
            }
        }
    }



    // Toast message zurücksetzen
    fun clearMessage() {
        message = ""
    }


    // Client Validierung
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