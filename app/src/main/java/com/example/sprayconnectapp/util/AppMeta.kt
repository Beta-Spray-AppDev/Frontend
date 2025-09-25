// AppMeta.kt
package com.example.sprayconnectapp.util

import android.os.Build

object AppMeta {
    const val VERSION = "1.0_BETA"  //

    fun deviceInfo(): String =
        "${Build.MANUFACTURER} ${Build.MODEL} (SDK ${Build.VERSION.SDK_INT})"
}
