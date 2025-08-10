package com.example.sprayconnectapp.navigation
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
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
import com.example.sprayconnectapp.ui.screens.BoulderView.ViewBoulderScreen

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
        composable(
            route = "boulders/{spraywallId}/{spraywallName}?imageUri={imageUri}",
            arguments = listOf(
                navArgument("spraywallId") { type = NavType.StringType },
                navArgument("spraywallName") { type = NavType.StringType },
                navArgument("imageUri") { type = NavType.StringType; nullable = true; defaultValue = null }
            )
        ) { backStackEntry ->
            val spraywallId = backStackEntry.arguments?.getString("spraywallId") ?: ""
            val spraywallName = backStackEntry.arguments?.getString("spraywallName") ?: ""
            val imageUri = backStackEntry.arguments?.getString("imageUri")
            BoulderListScreen(navController, spraywallId, spraywallName, imageUri)
        }

        composable(
            route = "addSpraywall/{gymId}/{gymName}",
            arguments = listOf(
                navArgument("gymId")   { type = NavType.StringType },
                navArgument("gymName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val gymId   = backStackEntry.arguments?.getString("gymId").orEmpty()
            val gymName = backStackEntry.arguments?.getString("gymName").orEmpty()
            AddSpraywallScreen(navController, gymId, gymName)
        }

        composable("addGym") {
            AddGymScreen(navController = navController)
        }

        // NavGraph.kt
        composable(
            route = "create_boulder/{spraywallId}?imageUri={imageUri}",
            arguments = listOf(
                navArgument("spraywallId") { type = NavType.StringType },
                navArgument("imageUri") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val spraywallId = backStackEntry.arguments?.getString("spraywallId")!!
            val imageUri = backStackEntry.arguments?.getString("imageUri")!!
            CreateBoulderScreen(
                spraywallId = spraywallId,
                imageUri = imageUri,
                onSave = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "boulders/{spraywallId}/{spraywallName}?imageUri={imageUri}",
            arguments = listOf(
                navArgument("spraywallId") { type = NavType.StringType },
                navArgument("spraywallName") { type = NavType.StringType },
                navArgument("imageUri") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val spraywallId = backStackEntry.arguments?.getString("spraywallId") ?: ""
            val spraywallName = backStackEntry.arguments?.getString("spraywallName") ?: ""
            val imageUri = backStackEntry.arguments?.getString("imageUri")

            BoulderListScreen(
                navController = navController,
                spraywallId = spraywallId,
                spraywallName = spraywallName,
                imageUri = imageUri

            )
        }

        composable(
            "spraywallDetail/{gymId}/{gymName}",
            arguments = listOf(
                navArgument("gymId") { type = NavType.StringType },
                navArgument("gymName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val gymId = backStackEntry.arguments?.getString("gymId").orEmpty()
            val gymName = backStackEntry.arguments?.getString("gymName").orEmpty()
            SpraywallDetailScreen(navController, gymId, gymName)
        }

        composable(
            route = "view_boulder/{boulderId}/{imageUri}",
            arguments = listOf(
                navArgument("boulderId") { type = NavType.StringType },
                navArgument("imageUri") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val boulderId = backStackEntry.arguments?.getString("boulderId").orEmpty()
            val imageUri = backStackEntry.arguments?.getString("imageUri").orEmpty()

            ViewBoulderScreen(
                boulderId = boulderId,
                imageUri = imageUri,
                onBack = { navController.popBackStack() }
            )
        }












    }

}