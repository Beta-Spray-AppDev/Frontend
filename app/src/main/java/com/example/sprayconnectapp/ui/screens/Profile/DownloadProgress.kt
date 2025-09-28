package com.example.sprayconnectapp.ui.screens.Profile


import android.app.DownloadManager
import android.content.Context
import android.net.Uri

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding

import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text

import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

import kotlinx.coroutines.delay


@Composable
fun DownloadProgress(
    downloadId: Long,
    onFinished: (Uri) -> Unit,
    onFailed: (() -> Unit)? = null,
    pollMillis: Long = 500
) {
    val ctx = LocalContext.current
    var progress by remember { mutableStateOf<Float?>(null) } // 0f..1f, null wenn unbekannt
    var statusText by remember { mutableStateOf("Download gestartet …") }

    LaunchedEffect(downloadId) {
        val dm = ctx.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val query = DownloadManager.Query().setFilterById(downloadId)

        while (true) {
            val c = dm.query(query) ?: break
            if (!c.moveToFirst()) {
                c.close()
                break
            }

            val status = c.getInt(c.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
            val downloaded = c.getLong(c.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
            val total = c.getLong(c.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
            val reason = c.getInt(c.getColumnIndexOrThrow(DownloadManager.COLUMN_REASON))

            progress = if (total > 0) downloaded.toFloat() / total.toFloat() else null

            statusText = when (status) {
                DownloadManager.STATUS_PENDING -> "Wartet …"
                DownloadManager.STATUS_RUNNING -> if (progress != null) {
                    "Lädt … ${(progress!! * 100).toInt()}%"
                } else {
                    "Lädt …"
                }

                DownloadManager.STATUS_PAUSED -> when (reason) {
                    DownloadManager.PAUSED_QUEUED_FOR_WIFI -> "Pausiert – wartet auf WLAN "
                    DownloadManager.PAUSED_WAITING_FOR_NETWORK -> "Pausiert – keine Verbindung "
                    DownloadManager.PAUSED_WAITING_TO_RETRY -> "Pausiert – versuche erneut "
                    else -> "Pausiert "
                }

                DownloadManager.STATUS_SUCCESSFUL -> {
                    val uri = dm.getUriForDownloadedFile(downloadId)
                    c.close()
                    if (uri != null) {
                        onFinished(uri)
                    } else {
                        onFailed?.invoke()
                    }
                    break // Schleife beenden
                }

                DownloadManager.STATUS_FAILED -> {
                    val failText = when (reason) {
                        DownloadManager.ERROR_CANNOT_RESUME -> "Fehler – kann nicht fortsetzen "
                        DownloadManager.ERROR_DEVICE_NOT_FOUND -> "Fehler – Speicher nicht gefunden "
                        DownloadManager.ERROR_INSUFFICIENT_SPACE -> "Fehler – zu wenig Speicher "
                        DownloadManager.ERROR_HTTP_DATA_ERROR -> "Fehler – Datenübertragung "
                        DownloadManager.ERROR_UNHANDLED_HTTP_CODE -> "Fehler – Server-Antwort "
                        else -> "Download fehlgeschlagen "
                    }
                    statusText = failText
                    c.close()
                    onFailed?.invoke()
                    break
                }

                else -> "Unbekannt"
            }

            c.close()
            delay(pollMillis)
        }

    }

    // UI
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Update wird heruntergeladen", style = MaterialTheme.typography.titleMedium)
            if (progress != null) {
                LinearProgressIndicator(progress = progress!!)
                Text("${(progress!! * 100).toInt()} %")
            } else {
                LinearProgressIndicator() // indeterminate
            }
            Text(statusText)
        }
    }
}
