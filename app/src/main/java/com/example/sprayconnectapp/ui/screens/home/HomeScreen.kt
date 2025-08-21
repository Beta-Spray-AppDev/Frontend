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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.focus.focusProperties
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



        val BarColor = colorResource(id = R.color.hold_type_bar)


        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("SprayConnect") },
                    actions = {
                        IconButton(onClick = { navController.navigate("addGym") },
                            modifier = Modifier.focusProperties { canFocus = false }
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Neues Gym anlegen")
                        }
                    },
                    navigationIcon = {
                        IconButton(modifier = Modifier.focusProperties { canFocus = false }, onClick = {
                            viewModel.logout(context)
                            navController.navigate("login") {
                                popUpTo("home") { inclusive = true }
                            }
                        }) {
                            Icon(Icons.Default.ExitToApp, contentDescription = "Logout", modifier = Modifier.size(28.dp) )
                        }

                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = BarColor,
                        scrolledContainerColor = BarColor,
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White,
                        actionIconContentColor = Color.White

                    ),
                )
            },
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
