package com.example.sprayconnectapp.ui.screens.login

import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.example.sprayconnectapp.data.LoginRequest
import com.example.sprayconnectapp.network.RetrofitInstance
import kotlinx.coroutines.launch

import com.example.sprayconnectapp.util.saveTokenToPrefs



class LoginViewModel : ViewModel() {

    var username by mutableStateOf("")
        private set

    var password by mutableStateOf("")
        private set

    var message by mutableStateOf("")

    fun onUsernameChange(new: String) {
        username = new
    }

    fun onPasswordChange(new: String) {
        password = new
    }
    fun loginUser(context: Context) {
        viewModelScope.launch {
            try {
                val request = LoginRequest(
                    username = username,
                    password = password
                )

                val response = RetrofitInstance.getApi(context).login(request)

                if (response.isSuccessful) {
                    val token = response.body()

                    if (!token.isNullOrBlank() && token.startsWith("ey")) {
                        message = "Login erfolgreich"
                        saveTokenToPrefs(context, token)
                    } else {
                        message = "Login fehlgeschlagen: Ungültige Antwort"
                    }
                } else {
                    message = "Login fehlgeschlagen: ${response.code()}"
                }
            } catch (e: Exception) {
                message = "Netzwerkfehler: ${e.localizedMessage}"
                Log.e("LoginViewModel", "Fehler beim Login", e)
            }
        }
    }










}