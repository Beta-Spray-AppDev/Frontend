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


    var usernameError by mutableStateOf<String?>(null)
        private set
    var emailError by mutableStateOf<String?>(null)
        private set





    fun onEmailChange(new: String) {
        email = new
        emailError = null // Fehlermeldung zurücksetzen
        message = ""
    }

    fun onPasswordChange(new: String) {
        password = new
    }

    fun onUsernameChange(new: String) {
        username = new
        usernameError = null // Fehlermeldung zurücksetzen
        message = ""
    }

    fun registerUser(context: Context, onLoginSuccess: () -> Unit) {
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
                    when (errorBody) {
                        "username_taken" -> usernameError = "Benutzername ist bereits vergeben."
                        "email_taken" -> emailError = "E-Mail ist bereits vergeben."
                        else -> message = "Registrierung fehlgeschlagen: ${response.code()}"
                    }                }
            } catch (e: Exception) {
                message = "Netzwerkfehler: ${e.localizedMessage}"
                Log.e("RegisterViewModel", "Fehler bei Registrierung", e)
            }
        }
    }

}