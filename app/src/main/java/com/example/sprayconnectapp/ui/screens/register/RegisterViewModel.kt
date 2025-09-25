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


/**
 * ViewModel für Registrierung:
 * - Hält Eingaben & Feldfehler (username/email/password)
 * - Live-/Blur-Validierung für E-Mail
 * - Führt Registrierung durch, danach Auto-Login
 * - Übergibt Erfolg via Callback (damit Screen navigiert/toastet)
 */

class RegisterViewModel : ViewModel() {
    var email by mutableStateOf("")
        private set

    var password by mutableStateOf("")
        private set

    var username by mutableStateOf("")
        private set

    var message by mutableStateOf("") // Rückmeldung in UI



    // Feldbezogene Fehlermeldungen
    var usernameError by mutableStateOf<String?>(null)
        private set
    var emailError by mutableStateOf<String?>(null)
        private set

    var isLoading by mutableStateOf(false); private set

    var passwordError by mutableStateOf<String?>(null); private set



    // wurde email Feld schon angeklickt
    private var emailTouched by mutableStateOf(false)


    /** Einfache E-Mail-Pattern-Validierung */
    private fun isEmailValid(e: String): Boolean {
        val regex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$".toRegex()
        return e.matches(regex)
    }



    /** Steuert Button-Aktivierung. */
    fun canSubmit(): Boolean =
        username.isNotBlank() && email.isNotBlank() && emailError == null &&  isEmailValid(email) && password.isNotBlank() && !isLoading



    // validierung während eingabe
    fun onEmailChange(new: String) {
        email = new
        message = ""
        // Wenn User Feld schon berührt hat - live validieren
        if (emailTouched) {
            emailError = when {
                email.isBlank() -> "Bitte E-Mail eingeben."
                !isEmailValid(email) -> "Bitte gültige E-Mail eingeben."
                else -> null
            }
        } else {
            emailError = null
        }


    }


    // wird aufgerufen wenn Email Fokus verliert
    fun onEmailBlur() {
        emailError = if (email.isNotBlank() && !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            "Bitte gültige E-Mail eingeben."
        } else {
            null
        }
    }


    fun onPasswordChange(new: String) {
        password = new
    }

    fun onUsernameChange(new: String) {
        username = new
        usernameError = null // Fehlermeldung zurücksetzen
        message = ""
    }

    /**
     * Registriert den User und führt anschließend Auto-Login aus.
     * @param onLoginSuccess wird bei erfolgreichem Login ausgelöst (Screen navigiert dann weiter).
     */


    fun registerUser(context: Context, onLoginSuccess: () -> Unit) {

        // Clientseitig validieren
        onEmailBlur()
        var ok = true
        if (username.isBlank()) { usernameError = "Bitte Benutzernamen eingeben."; ok = false }
        if (email.isBlank()) { emailError = "Bitte E-Mail eingeben."; ok = false }
        else if (!isEmailValid(email)) { emailError = "Bitte gültige E-Mail eingeben."; ok = false }
        if (password.isBlank()) { passwordError = "Bitte Passwort eingeben."; ok = false }
        if (!ok) return


        viewModelScope.launch {
            try {
                val request = RegisterRequest(
                    username = username,
                    email = email,
                    password = password
                )
                val response = RetrofitInstance.getApi(context).register(request)

                if (response.isSuccessful) {

                    //Registrierung erfolgreich, jetzt automatisch einloggen
                    val loginRequest = com.example.sprayconnectapp.data.dto.LoginRequest(
                        username = username,
                        password = password
                    )

                    val loginResponse = RetrofitInstance.getApi(context).login(loginRequest)


                    if (loginResponse.isSuccessful) {

                        //Auto-Login
                        val token = loginResponse.body()

                        if (!token.isNullOrBlank()) {
                            com.example.sprayconnectapp.util.saveTokenToPrefs(context, token)
                            RetrofitInstance.resetRetrofit()
                            message = "Registrierung & Login erfolgreich"
                            onLoginSuccess()
                        } else {
                            message = "Registrierung ok, aber Login fehlgeschlagen"
                        }

                    }
                    else {
                        message = "Registrierung ok, aber Login fehlgeschlagen"
                    }



                } else {
                    val errorBody = response.errorBody()?.string()?.trim().orEmpty()
                    when{
                        errorBody.contains("username_taken", true) -> usernameError = "Benutzername ist bereits vergeben."
                        errorBody.contains("email_taken", true) -> emailError = "E-Mail ist bereits vergeben."

                        response.code() == 400 -> {
                            message = "Eingaben ungültig (400)."
                        }
                        else -> message = "Registrierung fehlgeschlagen: ${response.code()}"
                    }                }
            } catch (e: Exception) {
                message = "Netzwerkfehler: ${e.localizedMessage}"
                Log.e("RegisterViewModel", "Fehler bei Registrierung", e)
            }
        }
    }

}