package com.example.sprayconnectapp.util

import android.content.Context
import androidx.core.content.edit
import org.json.JSONObject
import android.util.Base64


/** Holt den gespeicherten JWT aus SharedPreferences (oder null). */

fun getTokenFromPrefs(context: Context): String? {
    val sharedPref = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
    return sharedPref.getString("jwt_token", null)
}

/** Speichert den JWT in SharedPreferences. */
fun saveTokenToPrefs(context: Context, token: String) {
    val sharedPref = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
    sharedPref.edit().putString("jwt_token", token).apply()
}

/** Entfernt den JWT (Logout). */
fun clearTokenFromPrefs(context: Context) {
    val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
    prefs.edit { remove("jwt_token") }
    android.util.Log.d("Prefs", "Token gelöscht")

}

/**
 * Extrahiert `userId` aus dem JWT-Payload.
 * @return User-ID als String oder null bei ungültigem Token.
 */
fun getUserIdFromToken(token: String): String? {
    return try {
        val parts = token.split(".")
        if (parts.size != 3) return null

        val payload = String(Base64.decode(parts[1], Base64.URL_SAFE))
        val json = JSONObject(payload)

        json.getString("userId")
    } catch (e: Exception) {
        null
    }
}


/**
 * Extrahiert den Username (`sub`) aus dem JWT-Payload.
 * @return Username oder null bei ungültigem Token.
 */

fun getUsernameFromToken(token: String): String? {
    return try {
        val parts = token.split(".")
        if (parts.size != 3) return null

        val payload = String(Base64.decode(parts[1], Base64.URL_SAFE))
        val json = JSONObject(payload)

        json.getString("sub")
    } catch (e: Exception) {
        null
    }
}



