package com.example.sprayconnectapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import com.example.sprayconnectapp.navigation.NavGraph

import com.example.sprayconnectapp.ui.theme.SprayConnectAppTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SprayConnectAppTheme {
                val navController = rememberNavController()
                NavGraph(navController = navController)
            }

        }
    }
}

