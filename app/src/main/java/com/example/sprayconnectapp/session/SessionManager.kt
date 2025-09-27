package com.example.sprayconnectapp.session


import android.content.Context
import com.example.sprayconnectapp.data.dto.LogoutRequest
import com.example.sprayconnectapp.network.RetrofitInstance
import com.example.sprayconnectapp.util.TokenStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SessionManager {

    /** Logout: Server best-effort + lokal aufräumen */
    fun logout(context: Context) {
        val store = TokenStore(context)
        val rt = store.refreshToken()

        // serverseitig widerrufen (best effort, nicht blockierend für UI)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (!rt.isNullOrBlank()) {
                    RetrofitInstance.getApi(context).logout(LogoutRequest(rt))
                }
            } catch (_: Exception) { /* offline? egal */ }
        }

        // lokal aufräumen
        store.clear()
        RetrofitInstance.resetRetrofit()
    }
}