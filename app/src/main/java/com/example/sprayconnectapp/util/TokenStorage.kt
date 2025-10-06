package com.example.sprayconnectapp.util

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import org.json.JSONObject

class TokenStore private constructor(
    private val prefs: SharedPreferences
) {

    companion object {
        private const val PREFS_FILE = "secure_auth_prefs"
        private const val KEYSET_VALUE = "secure_auth_prefs__androidx_security_crypto_encrypted_prefs_value_keyset"
        private const val KEYSET_KEY   = "secure_auth_prefs__androidx_security_crypto_encrypted_prefs_key_keyset"

        /** Erzeugt einen TokenStore; bei Keystore/Decrypt-Fehlern werden Keysets/Prefs geleert und sauber neu erstellt. */
        fun create(context: Context): TokenStore {
            return try {
                TokenStore(newEncryptedPrefs(context))
            } catch (e: Exception) {
                // Recovery: alle betroffenen SharedPrefs löschen und neu anlegen
                nukeKeysets(context)
                TokenStore(newEncryptedPrefs(context))
            }
        }

        private fun newEncryptedPrefs(context: Context): SharedPreferences {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            return EncryptedSharedPreferences.create(
                context,
                PREFS_FILE,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        }

        /** Löscht die Token-Prefs + die beiden Tink-Keyset-Dateien (nur diese, nicht alles andere). */
        fun nukeKeysets(context: Context) {
            context.getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE).edit { clear() }
            context.getSharedPreferences(KEYSET_VALUE, Context.MODE_PRIVATE).edit { clear() }
            context.getSharedPreferences(KEYSET_KEY,   Context.MODE_PRIVATE).edit { clear() }
        }
    }

    // --- Speicher-Funktionen ---
    fun accessToken(): String? = prefs.getString("access", null)
    fun refreshToken(): String? = prefs.getString("refresh", null)

    fun save(access: String, refresh: String) {
        prefs.edit {
            putString("access", access)
            putString("refresh", refresh)
        }
    }

    fun clear() {
        prefs.edit { clear() }
    }

    // --- JWT-Helfer ---
    fun getUserId(): String? = accessToken()?.let { extractClaim(it, "userId") }
    fun getUsername(): String? = accessToken()?.let { extractClaim(it, "sub") }

    fun accessTokenExpired(leewaySeconds: Long = 30): Boolean {
        val token = accessToken() ?: return true
        return jwtExpired(token, leewaySeconds)
    }

    private fun extractClaim(token: String, claim: String): String? {
        return try {
            val parts = token.split("."); if (parts.size != 3) return null
            val payload = String(Base64.decode(parts[1], Base64.URL_SAFE))
            JSONObject(payload).optString(claim, null)
        } catch (_: Exception) { null }
    }

    private fun jwtExpired(jwt: String, leeway: Long): Boolean {
        return try {
            val parts = jwt.split("."); if (parts.size != 3) return true
            val payload = String(Base64.decode(parts[1], Base64.URL_SAFE))
            val exp = JSONObject(payload).optLong("exp", 0L)
            if (exp == 0L) true else (System.currentTimeMillis() / 1000) >= (exp - leeway)
        } catch (_: Exception) { true }
    }

    fun isSuperUser(): Boolean =
        Superusers.isSuper(getUserId())

}
