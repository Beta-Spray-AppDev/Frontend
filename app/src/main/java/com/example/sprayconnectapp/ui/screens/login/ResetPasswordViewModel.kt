package com.example.sprayconnectapp.ui.screens.login

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sprayconnectapp.data.dto.ResetPasswordRequest
import com.example.sprayconnectapp.network.RetrofitInstance
import kotlinx.coroutines.launch
import com.example.sprayconnectapp.data.dto.ValidateTokenResponse


class ResetPasswordViewModel : ViewModel() {

    var token by mutableStateOf("")
        private set
    var tokenValid by mutableStateOf<Boolean?>(null) // null = unbekannt
        private set

    var newPassword by mutableStateOf("")
        private set
    var confirm by mutableStateOf("")
        private set

    var newPwError by mutableStateOf<String?>(null)
        private set
    var confirmError by mutableStateOf<String?>(null)
        private set

    var isLoading by mutableStateOf(false)
        private set

    var message by mutableStateOf("")
        private set

    fun applyToken(t: String) { token = t }

    fun onNewPasswordChange(p: String) {
        newPassword = p
        newPwError = null
        message = ""
    }

    fun onConfirmChange(p: String) {
        confirm = p
        confirmError = null
        message = ""
    }

    /** Token beim Öffnen des Screens prüfen */
    fun validateToken(context: Context) {
        if (token.isBlank()) {
            tokenValid = false
            message = "Ungültiger Link."
            return
        }
        viewModelScope.launch {
            try {
                isLoading = true
                val api = RetrofitInstance.getApi(context)
                val res = api.validateResetToken(token)
                tokenValid = res.isSuccessful && (res.body()?.valid == true)
                if (tokenValid != true) message = "Token ist ungültig oder abgelaufen."
            } catch (e: Exception) {
                tokenValid = false
                message = "Netzwerkfehler: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }

    private fun validatePasswords(): Boolean {
        var ok = true
        if (newPassword.length > 32) {
            newPwError = "Passwort darf höchstens 32 Zeichen lang sein."
            ok = false
        }
        if (confirm != newPassword) {
            confirmError = "Passwörter stimmen nicht überein."
            ok = false
        }
        return ok
    }

    /** Passwort wirklich setzen */
    fun submit(context: Context, onSuccess: () -> Unit) {
        if (isLoading) return
        if (!validatePasswords()) return

        viewModelScope.launch {
            try {
                isLoading = true
                val api = RetrofitInstance.getApi(context)
                val res = api.resetPassword(ResetPasswordRequest(token, newPassword))
                if (res.isSuccessful) {
                    message = "Passwort aktualisiert. Bitte einloggen."
                    onSuccess()
                } else {
                    message = "Zurücksetzen fehlgeschlagen (${res.code()})."
                }
            } catch (e: Exception) {
                message = "Netzwerkfehler: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }
}
