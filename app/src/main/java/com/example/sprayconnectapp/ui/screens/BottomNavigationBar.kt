package com.example.sprayconnectapp.ui.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.sprayconnectapp.R


/**
 * Untere Navigationsleiste mit 3 Zielen:
 * - Home
 * - "Neuer Boulder" (führt zum Zielauswahl-Flow)
 * - Profil
 *
 * Markiert das aktuell aktive Ziel anhand der Route im Backstack.
 */

@Composable
fun BottomNavigationBar(navController: NavController) {

    //aktueller Navigation-stack-Entry
    val currentBackStackEntry = navController.currentBackStackEntryAsState()
    val currentDestination = currentBackStackEntry.value?.destination?.route


    //Background-Color für Navigations-Leiste
    val BarColor = colorResource(id = R.color.hold_type_bar)


    //Home
    NavigationBar (
        containerColor = BarColor,
        contentColor = Color.White,){
        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Home") },
            selected = currentDestination?.startsWith("home") == true,
            onClick = { navController.navigate("home") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.White,
                selectedTextColor = Color.White,
                unselectedIconColor = Color.White.copy(alpha = 0.75f),
                unselectedTextColor = Color.White.copy(alpha = 0.75f),
                indicatorColor = Color(0xFF2A2A2A)
            )
        )


        // Schnellzugriff: Neuer Boulder (immer unselected – eigener Flow)
        NavigationBarItem(
            icon = { Icon(Icons.Default.Add, contentDescription = "Neuer Boulder") },
            label = { Text("Neuer Boulder") },
            selected = false,
            onClick = { navController.navigate("pickBoulderTarget") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.White,
                selectedTextColor = Color.White,
                unselectedIconColor = Color.White.copy(alpha = 0.75f),
                unselectedTextColor = Color.White.copy(alpha = 0.75f),
                indicatorColor = Color(0xFF2A2A2A)            )
        )


        // Profil
        NavigationBarItem(
            icon = { Icon(Icons.Default.Person, contentDescription = "Profil") },
            label = { Text("Profil") },
            selected = currentDestination?.startsWith("profile") == true,
            onClick = { navController.navigate("profile") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.White,
                selectedTextColor = Color.White,
                unselectedIconColor = Color.White.copy(alpha = 0.75f),
                unselectedTextColor = Color.White.copy(alpha = 0.75f),
                indicatorColor = Color(0xFF2A2A2A)            )
        )
    }
}
