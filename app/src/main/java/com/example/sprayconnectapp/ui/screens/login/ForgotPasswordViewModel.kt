package com.example.sprayconnectapp.ui.screens.login


import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sprayconnectapp.data.dto.ForgotPasswordRequest
import com.example.sprayconnectapp.network.RetrofitInstance
import kotlinx.coroutines.launch


class ForgotPasswordViewModel : ViewModel() {
    var email by mutableStateOf("")
        private set


    var emailError by mutableStateOf<String?>(null)
        private set


    var isLoading by mutableStateOf(false)
        private set


    var message by mutableStateOf("")
        private set


    fun onEmailChange(new: String) {
        email = new
        emailError = null
        message = ""
    }


    private fun validate(): Boolean {
        val e = email.trim()
        return if (e.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(e).matches()) {
            emailError = "Bitte eine gültige E‑Mail eingeben."
            false
        } else true
    }


    fun submit(context: Context) {
        if (isLoading) return
        if (!validate()) return
        viewModelScope.launch {
            try {
                isLoading = true
                val api = RetrofitInstance.getApi(context)
                val res = api.forgotPassword(ForgotPasswordRequest(email.trim()))
                if (res.isSuccessful) {
                    message = "Wenn die E‑Mail existiert, haben wir dir einen Link geschickt."
                } else {
                    message = "Anfrage fehlgeschlagen (${res.code()})."
                }
            } catch (e: Exception) {
                message = "Netzwerkfehler: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }
}