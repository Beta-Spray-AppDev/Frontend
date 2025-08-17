package com.example.sprayconnectapp.ui.screens.home

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import com.example.sprayconnectapp.R
import com.example.sprayconnectapp.ui.screens.BottomNavigationBar
import com.example.sprayconnectapp.ui.screens.Profile.ProfileViewModel
import com.example.sprayconnectapp.util.getTokenFromPrefs
import com.example.sprayconnectapp.util.getUsernameFromToken


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {

    val context = LocalContext.current
    val viewModel: HomeViewModel = viewModel()

    val profileViewModel: ProfileViewModel = viewModel()

    val token = getTokenFromPrefs(context)
    val username = token?.let { getUsernameFromToken(it) }

    val profile by profileViewModel.profile.collectAsState()


    Log.d("Auth", "Angemeldet als: $username")



    LaunchedEffect(Unit) {
        viewModel.initRepository(context)
        viewModel.loadGyms(context)
        profileViewModel.loadProfile(context)
        Log.d("HomeViewModel", "Lade Gyms...")
    }

    val fallback = token?.let { getUsernameFromToken(it) } ?: ""
    val displayName = profile?.username ?: fallback



    //Farbverlauf
    val screenBg = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF53535B),
            Color(0xFF767981),
            Color(0xFFA8ABB2)
        )
    )



    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(screenBg)
    ){



        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
                BottomNavigationBar(navController)
            }

        ) { innerPadding ->

            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "Hallo ${displayName}!",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(32.dp))

                if (viewModel.isLoading.value) {
                    CircularProgressIndicator(color = colorResource(R.color.button_normal))
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
                                    val id   = gym.id.toString()
                                    val name = Uri.encode(gym.name)
                                    navController.navigate("spraywallDetail/$id/$name")
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(Modifier.padding(16.dp)) {
                                    Text(gym.name, style = MaterialTheme.typography.titleMedium)
                                    Text(gym.location, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }

                    }
                }


            }
        }




    }




}
