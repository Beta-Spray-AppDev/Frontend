package com.example.sprayconnectapp.ui.screens.register

import androidx.lifecycle.ViewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class RegisterViewModel : ViewModel() {
    var email by mutableStateOf("")
        private set

    var password by mutableStateOf("")
        private set

    fun onEmailChange(new: String) {
        email = new
    }

    fun onPasswordChange(new: String) {
        password = new
    }
}