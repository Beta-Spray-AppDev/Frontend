package com.example.sprayconnectapp.ui.screens.Profile

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController) {
    val context = LocalContext.current
    val viewModel: ProfileViewModel = viewModel()

    // Profile, Ladezustand & Fehler
    val profile by viewModel.profile.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    // Profil laden bei erstem Anzeigen
    LaunchedEffect(Unit) {
        viewModel.loadProfile(context)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mein Profil") },
                navigationIcon = {IconButton(onClick = {navController.popBackStack()}) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Zurück")
                }}

            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(24.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator()
                }

                error != null -> {
                    Text("Fehler: $error", color = MaterialTheme.colorScheme.error)
                }

                profile != null -> {
                    Text("Benutzername:", style = MaterialTheme.typography.labelLarge)
                    Text(profile!!.username, style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(16.dp))

                    Text("E-Mail:", style = MaterialTheme.typography.labelLarge)
                    Text(
                        profile!!.email ?: "Keine E-Mail hinterlegt",
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = { navController.navigate("editProfile") },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Bearbeiten")
                    }

                }

                else -> {
                    Text("Keine Profildaten vorhanden.")
                }
            }
        }
    }
}