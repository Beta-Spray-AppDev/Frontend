package com.example.sprayconnectapp.navigation
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.sprayconnectapp.ui.screens.LoginScreen
import com.example.sprayconnectapp.ui.screens.RegisterScreen
import com.example.sprayconnectapp.ui.screens.StartScreen

@Composable
fun NavGraph (navController: NavHostController){
    NavHost(
    navController = navController,
    startDestination = "start"
    ){
        composable("start"){ StartScreen(navController) }
        composable("login"){ LoginScreen() }
        composable("register"){ RegisterScreen() }
    }

}