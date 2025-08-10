// app/src/main/java/com/example/sprayconnectapp/util/DownloadHelpers.kt
package com.example.sprayconnectapp.util

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log
import java.io.File
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.HttpUrl.Companion.toHttpUrl
import okio.buffer
import okio.sink

private const val DL_TAG = "SprayDL"

// -----------------------------
// Namens-/URL-Helfer (Nextcloud)
// -----------------------------

/** Fallback-Dateiname aus Token, wenn aus der Preview-URL kein echter Name hervorgeht. */
fun sprayFileName(token: String): String = "spray_${token}.jpg"

/**
 * Baut aus einer Nextcloud-Preview-URL die korrekte Download-URL.
 * - Single-File:  https://host/index.php/s/<TOKEN>/preview
 *                  -> https://host/index.php/s/<TOKEN>/download
 * - Ordner-Share: https://host/index.php/s/<TOKEN>/preview?file=/sub/001.jpg
 *                  -> https://host/index.php/s/<TOKEN>/download?path=%2Fsub&files=001.jpg
 */
fun buildDownloadUrlFromPreview(previewUrl: String): String? {
    val token = Regex("/s/([^/]+)/").find(previewUrl)?.groupValues?.get(1) ?: return null
    val uri = Uri.parse(previewUrl)
    val fileParam = uri.getQueryParameter("file") // z. B. "/001.jpg" oder "/sub/abc.jpg"

    return if (!fileParam.isNullOrBlank()) {
        val lastSlash = fileParam.lastIndexOf('/')
        val dir  = if (lastSlash <= 0) "/" else fileParam.substring(0, lastSlash)
        val name = fileParam.substring(lastSlash + 1)
        val encPath = Uri.encode(dir)
        val encFile = Uri.encode(name)
        "https://leitln.at/maltacloud/index.php/s/$token/download?path=$encPath&files=$encFile"
    } else {
        "https://leitln.at/maltacloud/index.php/s/$token/download"
    }
}

/** Lokaler Dateiname: echtes `file=` aus Preview oder Fallback mit Token. */
fun localOutputNameFromPreview(previewUrl: String, tokenFallback: String): String {
    val uri = Uri.parse(previewUrl)
    val fileParam = uri.getQueryParameter("file")
    return if (!fileParam.isNullOrBlank()) {
        val name = fileParam.substringAfterLast('/').ifBlank { sprayFileName(tokenFallback) }
        name
    } else {
        sprayFileName(tokenFallback)
    }
}

/** Referer-Header für manche Nextcloud-Setups hilfreich. */
fun refererFromPreview(previewUrl: String): String? {
    val token = Regex("/s/([^/]+)/").find(previewUrl)?.groupValues?.get(1) ?: return null
    return "https://leitln.at/maltacloud/index.php/s/$token/preview"
}

// -----------------------------
// Datei-Helfer (App-privater Ordner)
// -----------------------------

fun getPrivateImageFileByName(context: Context, outName: String): File {
    val dir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        ?: throw IllegalStateException("Pictures dir unavailable")
    if (!dir.exists()) dir.mkdirs()
    return File(dir, outName)
}

fun getPrivateImageUriByName(context: Context, outName: String): Uri =
    Uri.fromFile(getPrivateImageFileByName(context, outName))

// Backwards-Compat: Zugriff per Token
fun getPrivateImageFile(context: Context, token: String): File {
    val dir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    return File(dir, sprayFileName(token))
}
fun getPrivateImageUri(context: Context, token: String): Uri =
    Uri.fromFile(getPrivateImageFile(context, token))

// -----------------------------
// Silent Direct Download (OkHttp) mit Redirect-Fix
// -----------------------------

private val http by lazy {
    OkHttpClient.Builder()
        .followRedirects(false)    // Redirects behandeln wir selbst (http -> https hochbiegen)
        .followSslRedirects(false)
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()
}

private fun buildRequest(url: String, referer: String?): Request {
    val b = Request.Builder()
        .url(url)
        .header("User-Agent", "SprayConnect/1.0 (Android)")
        .header("Accept", "image/*")
    if (referer != null) b.header("Referer", referer)
    return b.build()
}

/**
 * Lädt `url` leise in den app-privaten Pictures-Ordner.
 * Kein DownloadManager, keine Notification. Gibt immer eine file://-Uri zurück
 * oder wirft eine Exception bei Fehlern.
 */
suspend fun downloadDirectToPrivate(
    context: Context,
    url: String,
    outName: String,
    referer: String? = null
): Uri = withContext(Dispatchers.IO) {
    Log.i(DL_TAG, "HTTP GET $url -> $outName")
    var currentUrl = url

    repeat(6) { // max. 6 Redirects
        http.newCall(buildRequest(currentUrl, referer)).execute().use { resp ->
            val code = resp.code

            // 30x: Redirect manuell behandeln
            if (code in 300..399) {
                val loc = resp.header("Location")
                    ?: throw IllegalStateException("Redirect ohne Location")

                currentUrl = when {
                    // http -> https hochstufen (vermeidet Cleartext-Blocker)
                    loc.startsWith("http://") -> loc.replaceFirst("http://", "https://")
                    // relative Location -> gegen aktuelle URL auflösen
                    loc.startsWith("/") -> {
                        val base = currentUrl.toHttpUrl()
                        base.resolve(loc)?.toString()
                            ?: throw IllegalStateException("Ungültiger Redirect: $loc")
                    }
                    else -> loc
                }
                // nächste Redirect-Runde
                return@use
            }

            if (!resp.isSuccessful) throw IllegalStateException("HTTP $code")

            // Erfolg -> Datei speichern
            val outFile = getPrivateImageFileByName(context, outName)
            val body = resp.body ?: throw IllegalStateException("Empty body")
            body.source().use { src ->
                outFile.sink().buffer().use { sink -> sink.writeAll(src) }
            }
            return@withContext Uri.fromFile(outFile)
        }
    }

    throw IllegalStateException("Zu viele Redirects")
}
