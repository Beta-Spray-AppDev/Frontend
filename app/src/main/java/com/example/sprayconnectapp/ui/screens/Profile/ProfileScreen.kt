package com.example.sprayconnectapp.ui.screens.Profile

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.sprayconnectapp.data.dto.BoulderDTO
import com.example.sprayconnectapp.data.dto.UserProfile
import com.example.sprayconnectapp.ui.screens.BottomNavigationBar

import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController) {
    val context = LocalContext.current
    val viewModel: ProfileViewModel = viewModel()

    val profile by viewModel.profile.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val boulders by viewModel.myBoulders.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadProfile(context)
        viewModel.loadMyBoulders(context)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                    title = { Text("Mein Profil") },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Zurück")
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            viewModel.logout(context)
                            navController.navigate("login") {
                                popUpTo("home") { inclusive = true }
                            }
                        }) {
                            Icon(Icons.Default.ExitToApp, contentDescription = "Logout", modifier = Modifier.size(28.dp) )
                        }
                    }

            )

        },
        bottomBar = { BottomNavigationBar(navController) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                when {
                    isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }

                    error != null -> {
                        Text("Fehler: $error", color = MaterialTheme.colorScheme.error)
                    }

                    profile != null -> {
                        ProfileCard(profile = profile!!, navController = navController)
                        Spacer(modifier = Modifier.height(17.dp))

                        BoulderListCard(boulders = boulders, navController = navController)

                    }

                    else -> {
                        Text("Keine Profildaten vorhanden.")
                    }
                }
            }
        }
    }
}


@Composable
fun ProfileInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium
        )
    }
}




@Composable
fun ProfileCard(profile: UserProfile, navController: NavController) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Nutzerdaten", style = MaterialTheme.typography.headlineSmall)
            Divider()
            ProfileInfoRow(label = "Benutzername", value = profile.username)
            ProfileInfoRow(label = "E-Mail", value = profile.email ?: "Keine E-Mail hinterlegt")

            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { navController.navigate("editProfile") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Profil bearbeiten")
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoulderCard(boulder: BoulderDTO, onClick: (() -> Unit)? = null) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(4.dp),
        onClick = {
             onClick?.invoke()
        }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(boulder.name, style = MaterialTheme.typography.titleMedium)
            Text("Schwierigkeit: ${boulder.difficulty}", style = MaterialTheme.typography.bodyMedium)
        }
    }
}



@Composable
fun BoulderListCard(boulders: List<BoulderDTO>, navController: NavController) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Meine Boulder", style = MaterialTheme.typography.headlineSmall)
            Divider()

            if (boulders.isEmpty()) {
                Text("Keine Boulder erstellt.")
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    boulders.forEach { boulder ->
                        BoulderCard(boulder,
                            onClick = {
                                val imageUri = boulder.spraywallImageUrl ?: ""
                                val route = "view_boulder/${boulder.id}/${boulder.spraywallId}/${Uri.encode(imageUri)}"
                                navController.navigate(route)
                        }
                        )
                    }
                }
            }
        }
    }
}




