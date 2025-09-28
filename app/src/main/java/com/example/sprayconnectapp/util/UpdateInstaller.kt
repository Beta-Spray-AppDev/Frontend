package com.example.sprayconnectapp.util

import android.app.DownloadManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.widget.Toast
import androidx.core.net.toUri

object UpdateInstaller {

    fun enqueueDownload(ctx: Context, rawUrl: String): Long? {
        val url = normalizeApkUrl(rawUrl) ?: return null

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val pm = ctx.packageManager
            if (!pm.canRequestPackageInstalls()) {
                val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES)
                    .setData("package:${ctx.packageName}".toUri())
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                ctx.startActivity(intent)
                Toast.makeText(ctx, "Bitte Installation aus unbekannten Quellen erlauben und erneut versuchen.", Toast.LENGTH_LONG).show()
                return null
            }
        }

        val fileName = "sprayconnect-latest.apk"
        val req = DownloadManager.Request(Uri.parse(url))
            .setTitle("SprayConnect Update")
            .setDescription("Neue Version wird heruntergeladen…")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)
            .setMimeType("application/vnd.android.package-archive")

        val dm = ctx.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val downloadId = dm.enqueue(req)

        UpdatePrefs.saveDownloadId(ctx, downloadId) // für spätere Checks/Banner
        return downloadId
    }

    // muss öffentlich sein, damit der Receiver sie nutzen kann
    fun startPackageInstaller(ctx: Context, apkUri: Uri) {
        val install = Intent(Intent.ACTION_VIEW)
            .setDataAndType(apkUri, "application/vnd.android.package-archive")
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            ctx.startActivity(install)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(ctx, "Kein Installer gefunden.", Toast.LENGTH_LONG).show()
        }
    }

    fun normalizeApkUrl(src: String?): String? {
        if (src.isNullOrBlank()) return null
        return if (src.contains("/s/") && !src.endsWith("/download")) {
            src.trimEnd('/') + "/download"
        } else src
    }


    fun findCompletedApkUri(ctx: Context): Uri? {
        val id = UpdatePrefs.getDownloadId(ctx)
        if (id <= 0L) return null

        val dm = ctx.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val q = DownloadManager.Query().setFilterById(id)
        dm.query(q)?.use { c ->
            if (c.moveToFirst()) {
                val statusIdx = c.getColumnIndex(DownloadManager.COLUMN_STATUS)
                if (statusIdx != -1 && c.getInt(statusIdx) == DownloadManager.STATUS_SUCCESSFUL) {
                    return dm.getUriForDownloadedFile(id)
                }
            }
        }
        return null
    }

}
