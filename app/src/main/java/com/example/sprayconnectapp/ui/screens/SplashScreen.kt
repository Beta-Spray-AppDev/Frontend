package com.example.sprayconnectapp.ui.screens

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.navigation.NavController
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import com.example.sprayconnectapp.R
import com.example.sprayconnectapp.data.dto.RefreshRequest
import com.example.sprayconnectapp.network.RetrofitInstance
import com.example.sprayconnectapp.util.TokenStore

@Composable
fun SplashScreen(nav: NavController) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        val store = TokenStore.create(context)


        // 1) Access-Token noch gültig?
        if (!store.accessTokenExpired()) {
            nav.navigate("home") { popUpTo("splash") { inclusive = true } }
            return@LaunchedEffect
        }

        // 2) Refresh versuchen
        val rt = store.refreshToken()
        if (rt.isNullOrBlank()) {
            nav.navigate("login") { popUpTo("splash") { inclusive = true } }
            return@LaunchedEffect
        }

        try {
            val res = RetrofitInstance.getApi(context).refresh(RefreshRequest(rt))
            if (res.isSuccessful && res.body() != null) {
                val t = res.body()!!
                store.save(t.accessToken, t.refreshToken)
                nav.navigate("home") { popUpTo("splash") { inclusive = true } }
            } else {
                nav.navigate("login") { popUpTo("splash") { inclusive = true } }
            }
        } catch (_: Exception) {
            nav.navigate("login") { popUpTo("splash") { inclusive = true } }
        }
    }

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(
            color = colorResource(R.color.button_normal)
        )
    }
}
