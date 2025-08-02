package com.example.sprayconnectapp.navigation
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.sprayconnectapp.ui.screens.login.LoginScreen
import com.example.sprayconnectapp.ui.screens.register.RegisterScreen
import com.example.sprayconnectapp.ui.screens.StartScreen
import com.example.sprayconnectapp.ui.screens.home.HomeScreen

@Composable
fun NavGraph (navController: NavHostController){
    NavHost(
    navController = navController,
    startDestination = "start"
    ){
        composable("start"){ StartScreen(navController) }
        composable("login"){ LoginScreen(navController) }
        composable("register"){ RegisterScreen(navController) }
        composable("home") { HomeScreen(navController) }

    }

}