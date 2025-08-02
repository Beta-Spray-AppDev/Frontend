package com.example.sprayconnectapp.ui.screens.home

import android.content.Context
import androidx.lifecycle.ViewModel
import com.example.sprayconnectapp.util.clearTokenFromPrefs

class HomeViewModel : ViewModel() {

    fun logout(context: Context) {
        clearTokenFromPrefs(context)
    }

}