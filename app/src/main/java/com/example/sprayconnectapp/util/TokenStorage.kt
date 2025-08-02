package com.example.sprayconnectapp.util

import android.content.Context
import androidx.core.content.edit

fun getTokenFromPrefs(context: Context): String? {
    val sharedPref = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
    return sharedPref.getString("jwt_token", null)
}

fun saveTokenToPrefs(context: Context, token: String) {
    val sharedPref = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
    sharedPref.edit().putString("jwt_token", token).apply()
}

fun clearTokenFromPrefs(context: Context) {
    val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
    prefs.edit { remove("jwt_token") }
    android.util.Log.d("Prefs", "Token gelöscht")

}

