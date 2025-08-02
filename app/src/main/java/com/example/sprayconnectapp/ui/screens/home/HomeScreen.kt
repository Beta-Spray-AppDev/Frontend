package com.example.sprayconnectapp.ui.screens.home

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

import androidx.compose.foundation.lazy.items


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {

    val context = LocalContext.current

    val viewModel: HomeViewModel = viewModel()

    LaunchedEffect(Unit) {
        viewModel.loadGyms(context)
        Log.d("HomeViewModel", "Lade Gyms...")
    }



    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Willkommen bei SprayConnect!",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(32.dp))



        if (viewModel.isLoading.value) {
            CircularProgressIndicator()
        } else if (viewModel.errorMessage.value != null) {
            Text("Fehler: ${viewModel.errorMessage.value}", color = MaterialTheme.colorScheme.error)
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(viewModel.gyms.value) { gym ->
                    Card(
                        onClick = {
                            // TODO: Gym auswählen und weiterleiten zu spez. Gym
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(gym.name, style = MaterialTheme.typography.titleMedium)
                            Text(gym.location, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }

        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                viewModel.logout(context)
                navController.navigate("login") {
                    popUpTo("home") { inclusive = true }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Logout")
        }
    }
}
