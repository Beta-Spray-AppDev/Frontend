package com.example.sprayconnectapp.ui.screens.Profile

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.sprayconnectapp.ui.screens.BottomNavigationBar
import com.example.sprayconnectapp.ui.screens.Profile.ProfileViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(navController: NavController) {
    val context = LocalContext.current
    val viewModel: ProfileViewModel = viewModel()

    //Viewmodel state
    val profile by viewModel.profile.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    // Eingabefelder
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // UI-Meldungen
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var infoMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadProfile(context)
    }

    // Nur beim ersten Mal mit aktuellen Werten befüllen
    LaunchedEffect(profile) {
        profile?.let {
            username = it.username
            email = it.email ?: ""
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profil bearbeiten") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Zurück")
                    }
                }
            )
        },
        bottomBar = {BottomNavigationBar(navController)}
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(24.dp)
                .fillMaxSize()
        ) {
            // Username Eingabe
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Benutzername") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))


            // Email Eingabe
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("E-Mail") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))


            // Passwort Eingabe
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Neues Passwort") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Speichern-Button
            Button(
                onClick = {
                    viewModel.updateProfile(
                        context,
                        username,
                        email,
                        password,
                        onSuccess = {

                            // Zurücknavigieren + Toast anzeigen
                            navController.popBackStack()
                            Toast.makeText(context, "Profil gespeichert ", Toast.LENGTH_LONG).show()

                        },
                        onError = {
                            errorMessage = it

                        }
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Speichern")
            }

            Spacer(modifier = Modifier.height(8.dp))

            // wie Profil aktualisiert
            infoMessage?.let {
                Text(it, color = MaterialTheme.colorScheme.primary)
            }

            // Fehlertext
            errorMessage?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            if (isLoading) {
                CircularProgressIndicator()
            }

            error?.let {
                Text("Fehler: $it", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}
