package com.example.sprayconnectapp.navigation
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.sprayconnectapp.ui.screens.BoulderList.BoulderListScreen
import com.example.sprayconnectapp.ui.screens.BoulderView.CreateBoulderScreen
import com.example.sprayconnectapp.ui.screens.spraywall.AddSpraywallScreen
import com.example.sprayconnectapp.ui.screens.GymDetail.GymDetailScreen
import com.example.sprayconnectapp.ui.screens.login.LoginScreen
import com.example.sprayconnectapp.ui.screens.register.RegisterScreen
import com.example.sprayconnectapp.ui.screens.StartScreen
import com.example.sprayconnectapp.ui.screens.home.AddGymScreen
import com.example.sprayconnectapp.ui.screens.home.HomeScreen
import com.example.sprayconnectapp.ui.screens.spraywall.SpraywallDetailScreen

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
        composable("gymDetail/{gymId}/{gymName}") { backStackEntry ->
            val gymId = backStackEntry.arguments?.getString("gymId") ?: ""
            val gymName = backStackEntry.arguments?.getString("gymName") ?: ""
            GymDetailScreen(navController = navController, gymId = gymId, gymName = gymName)

        }
        composable("spraywallDetail/{gymId}/{gymName}") { backStackEntry ->
            val gymId = backStackEntry.arguments?.getString("gymId") ?: ""
            val gymName = backStackEntry.arguments?.getString("gymName") ?: ""
            SpraywallDetailScreen(navController, gymId, gymName)
        }
        composable("addSpraywall/{gymId}/{gymName}") { backStackEntry ->
            val gymId = backStackEntry.arguments?.getString("gymId") ?: ""
            val gymName = backStackEntry.arguments?.getString("gymName") ?: ""
            AddSpraywallScreen(navController = navController, gymId = gymId, gymName = gymName)
        }
        composable("addGym") {
            AddGymScreen(navController = navController)
        }

        composable("create_boulder/{spraywallId}") { backStackEntry ->
            val spraywallId = backStackEntry.arguments?.getString("spraywallId") ?: ""
            CreateBoulderScreen(
                spraywallId = spraywallId,
                onSave = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }

        composable("boulders/{spraywallId}/{spraywallName}") { backStackEntry ->
            val spraywallId = backStackEntry.arguments?.getString("spraywallId") ?: ""
            val spraywallName = backStackEntry.arguments?.getString("spraywallName") ?: ""
            BoulderListScreen(navController, spraywallId, spraywallName)
        }








    }

}