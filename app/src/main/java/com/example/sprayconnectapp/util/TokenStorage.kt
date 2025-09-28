package com.example.sprayconnectapp.util

import android.content.Context
import androidx.core.content.edit
import org.json.JSONObject
import android.util.Base64


import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys


public class TokenStore(context: Context){


    private val masterKey = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)


    private val prefs = EncryptedSharedPreferences.create(
        "secure_auth_prefs",              // Datei-Name
        masterKey,                        // Master-Key im Android Keystore
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    private val SUPERUSER_IDS = setOf(
        "9d6305c6-0052-48da-8fa9-3ee162260812",
        "842a8734-4125-4d25-9a88-48ba0520ac91",
        "0b6ae861-1f72-4cc4-861b-aa9ff8421e45",
        "e1232010-c83a-494f-9cb7-4ce8f503d75a"
    )



    /** --- Speicher-Funktionen --- */

    fun accessToken(): String? = prefs.getString("access", null)

    fun refreshToken(): String? = prefs.getString("refresh", null)

    fun save(access: String, refresh: String) {
        prefs.edit {
            putString("access", access)
                .putString("refresh", refresh)
        }
    }

    fun clear() {
        prefs.edit { clear() }
    }


    /** --- Decode-Helfer fürs JWT --- */

    fun getUserId(): String? {
        val token = accessToken() ?: return null
        return extractClaim(token, "userId")
    }

    fun getUsername(): String? {
        val token = accessToken() ?: return null
        return extractClaim(token, "sub")
    }


    /** --- Private Helfer zum Claim-Auslesen --- */
    private fun extractClaim(token: String, claim: String): String? {
        return try {
            val parts = token.split(".")
            if (parts.size != 3) return null
            val payload = String(Base64.decode(parts[1], Base64.URL_SAFE))
            val json = JSONObject(payload)
            json.getString(claim)
        } catch (e: Exception) {
            null
        }
    }

    fun isSuperUser(): Boolean {
        val userId = getUserId() ?: return false
        return SUPERUSER_IDS.contains(userId)
    }



}
