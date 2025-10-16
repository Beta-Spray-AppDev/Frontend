package com.example.sprayconnectapp.navigation
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.example.sprayconnectapp.ui.screens.BoulderList.BoulderListScreen
import com.example.sprayconnectapp.ui.screens.BoulderView.BoulderScreenMode
import com.example.sprayconnectapp.ui.screens.BoulderView.CreateBoulderScreen
import com.example.sprayconnectapp.ui.screens.BoulderView.PickBoulderTargetScreen
import com.example.sprayconnectapp.ui.screens.spraywall.AddSpraywallScreen
import com.example.sprayconnectapp.ui.screens.GymDetail.GymDetailScreen
import com.example.sprayconnectapp.ui.screens.Profile.EditProfileScreen
import com.example.sprayconnectapp.ui.screens.Profile.ProfileScreen
import com.example.sprayconnectapp.ui.screens.login.LoginScreen
import com.example.sprayconnectapp.ui.screens.register.RegisterScreen
import com.example.sprayconnectapp.ui.screens.StartScreen
import com.example.sprayconnectapp.ui.screens.home.AddGymScreen
import com.example.sprayconnectapp.ui.screens.home.HomeScreen
import com.example.sprayconnectapp.ui.screens.spraywall.SpraywallDetailScreen
import com.example.sprayconnectapp.ui.screens.BoulderView.ViewBoulderScreen
import com.example.sprayconnectapp.ui.screens.SplashScreen
import com.example.sprayconnectapp.ui.screens.comments.BoulderCommentsScreen
import com.example.sprayconnectapp.ui.screens.login.ForgotPasswordScreen
import com.example.sprayconnectapp.ui.screens.login.ResetPasswordScreen

@Composable
fun NavGraph (navController: NavHostController){
    NavHost(
    navController = navController,
    startDestination = "splash"
    ){
        // Einstiegs-/Auth-/Basisrouten
        composable("splash") { SplashScreen(navController) }
        composable("start"){ StartScreen(navController) }
        composable("login"){ LoginScreen(navController) }
        composable("register"){ RegisterScreen(navController) }
        composable("forgot") { ForgotPasswordScreen(navController) }
        composable("home") { HomeScreen(navController) }

        // Gym-Detail mit Path-Parametern
        composable("gymDetail/{gymId}/{gymName}") { backStackEntry ->
            val gymId = backStackEntry.arguments?.getString("gymId") ?: ""
            val gymName = backStackEntry.arguments?.getString("gymName") ?: ""
            GymDetailScreen(navController = navController, gymId = gymId, gymName = gymName)

        }

        composable(
            route = "reset?token={token}",
            arguments = listOf(
                navArgument("token") { type = NavType.StringType; defaultValue = "" }
            ),
            deepLinks = listOf(
                navDeepLink { uriPattern = "https://sprayconnect.at/reset?token={token}" }
            )
        ) { backStackEntry ->
            val token = backStackEntry.arguments?.getString("token")
            ResetPasswordScreen(navController, tokenArg = token)
        }

        /**
         * Boulder-Liste über eine Spraywall.
         */
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

        // Spraywall anlegen (Gym-Kontext als Pfadparameter)
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

        /**
         * Boulder erstellen/bearbeiten:
         * - Pflicht: spraywallId
         * - Optional (Query): imageUri, mode ("create"|"edit"), boulderId, fromPicker
         *   -> wird in BoulderScreenMode übersetzt
         */

        composable(
            "create_boulder/{spraywallId}?imageUri={imageUri}&mode={mode}&boulderId={boulderId}&fromPicker={fromPicker}",
            arguments = listOf(
                // Pflicht-Argument
                navArgument("spraywallId") { type = NavType.StringType },

                //Optionale Query-Argumente mit default-Werten
                navArgument("imageUri") { type = NavType.StringType; defaultValue = "" },
                navArgument("mode") { type = NavType.StringType; defaultValue = "create" },
                navArgument("boulderId") { type = NavType.StringType; defaultValue = "" },
                navArgument("fromPicker") { type = NavType.BoolType; defaultValue = false }

            )
        ) { backStackEntry ->

            // Werte aus den Argumenten lesen
            val spraywallId = backStackEntry.arguments?.getString("spraywallId") ?: ""
            val imageUri = backStackEntry.arguments?.getString("imageUri") ?: ""
            val mode = backStackEntry.arguments?.getString("mode") ?: "create"
            val boulderId = backStackEntry.arguments?.getString("boulderId") ?: ""
            val fromPicker = backStackEntry.arguments?.getBoolean("fromPicker") ?: false

            val screenMode = if (mode == "edit") {
                BoulderScreenMode.Edit(boulderId)
            } else {
                BoulderScreenMode.Create
            }

            CreateBoulderScreen(
                spraywallId = spraywallId,
                imageUri = imageUri,
                mode = screenMode,
                fromPicker = fromPicker,
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


        // Spraywall-Detail (Gym-Kontext)
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

        /**
         * Boulder-Detail/Viewer:
         * - Pflicht: boulderId, spraywallId
         * - Optional: src (Quelle der Navigation), imageUri
         */

        composable(
            "view_boulder/{boulderId}/{spraywallId}?src={src}&imageUri={imageUri}",
            arguments = listOf(
                navArgument("boulderId") { type = NavType.StringType },
                navArgument("spraywallId") { type = NavType.StringType },
                navArgument("imageUri") { type = NavType.StringType; defaultValue = "" },
                navArgument("src"){ type = NavType.StringType; defaultValue = "" }
            )
        ) { backStackEntry ->
            val boulderId = backStackEntry.arguments?.getString("boulderId") ?: ""
            val spraywallId = backStackEntry.arguments?.getString("spraywallId") ?: ""
            val imageUri = backStackEntry.arguments?.getString("imageUri") ?: ""
            val source      = backStackEntry.arguments?.getString("src") ?: "mine"

            ViewBoulderScreen(
                navController = navController,
                boulderId = boulderId,
                spraywallId = spraywallId,
                imageUri = imageUri,
                source = source,
                onBack = { navController.popBackStack() },
            )
        }

        // Auswahlziel für Boulder (wohin speichern)
        composable("pickBoulderTarget") {
            PickBoulderTargetScreen(
                navController = navController
            )
        }


        // Profilrouten

        composable("profile") {
            ProfileScreen(navController)
        }
        composable("editProfile") {
            EditProfileScreen(navController)
        }

        composable(
            route = "boulderComments/{boulderId}?boulderName={boulderName}",
            arguments = listOf(
                navArgument("boulderId") { type = NavType.StringType },
                navArgument("boulderName") { type = NavType.StringType; defaultValue = "" }
            )
        ) { backStackEntry ->
            val boulderId = backStackEntry.arguments?.getString("boulderId")!!
            val boulderName = backStackEntry.arguments?.getString("boulderName").orEmpty()

            BoulderCommentsScreen(
                navController = navController,
                boulderId = boulderId,
                boulderName = boulderName
            )
        }




    }

}