package com.example.sprayconnectapp.util

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class LatestRelease(
    val versionCode: Int,
    val versionName: String,
    val apkUrl: String?,
    val changelog: String?
)

object UpdateChecker {

    private val client = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .build()

    // --- WICHTIG: suspend + IO-Dispatcher ---
    suspend fun fetchLatest(url: String): LatestRelease? = withContext(Dispatchers.IO) {
        try {
            Log.d("Update", "GET $url")
            val request = Request.Builder().url(url).build()
            client.newCall(request).execute().use { response ->
                val body = response.body?.string()
                Log.d("Update", "HTTP ${response.code}")
                Log.d("Update", "BODY: $body")

                if (!response.isSuccessful || body.isNullOrBlank()) return@withContext null
                val json = JSONObject(body)

                LatestRelease(
                    versionCode = json.optInt("versionCode", -1),
                    versionName = json.optString("versionName", ""),
                    apkUrl = json.optString("apkUrl", null),
                    changelog = json.optString("changelog", null)
                )
            }
        } catch (e: Exception) {
            Log.e("Update", "fetchLatest failed", e)
            null
        }
    }
}
