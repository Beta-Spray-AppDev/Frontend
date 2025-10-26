package com.example.sprayconnectapp.ui.screens.BoulderView

import android.content.Context
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.ImageSearch
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.sprayconnectapp.R
import com.example.sprayconnectapp.data.dto.Gym
import com.example.sprayconnectapp.data.dto.SpraywallDTO
import com.example.sprayconnectapp.network.RetrofitInstance
import com.example.sprayconnectapp.ui.screens.BottomNavigationBar
import kotlinx.coroutines.launch
import java.util.*

/**
 * 2-stufiger Picker zum Anlegen eines neuen Boulders:
 * 1) Gym wählen → 2) Spraywall des Gyms wählen → navigiert zum Create-Screen.
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PickBoulderTargetScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var gyms by remember { mutableStateOf<List<Gym>>(emptyList()) }
    var spraywalls by remember { mutableStateOf<List<SpraywallDTO>>(emptyList()) }
    var selectedGym by remember { mutableStateOf<Gym?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }


    // Hintergrund mit Farbverlauf
    val screenBg = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF53535B),
            Color(0xFF767981),
            Color(0xFFA8ABB2)
        )
    )

    val BarColor = colorResource(id = R.color.hold_type_bar)

    // Gyms laden
    LaunchedEffect(Unit) {
        isLoading = true
        try {
            val res = RetrofitInstance.getGymApi(context).getAllGyms()
            if (res.isSuccessful) {
                gyms = res.body() ?: emptyList()
            } else {
                error = "Fehler beim Laden der Gyms (${res.code()})"
            }
        } catch (t: Throwable) {
            error = t.localizedMessage
        }
        isLoading = false
    }


    Box(
        Modifier
            .fillMaxSize()
            .background(screenBg)
    )
    {
        Scaffold(

            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = BarColor,
                        scrolledContainerColor = BarColor,
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White,
                        actionIconContentColor = Color.White
                    ),

                    // Schließen Button
                    navigationIcon = {
                        if (selectedGym == null) {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(Icons.Default.Close, contentDescription = "Abbrechen")
                            }
                        } else {
                            IconButton(onClick = { selectedGym = null }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Zurück zur Gym-Auswahl")
                            }
                        }
                    },
                    //Titel plus Hinweis
                    title = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "Boulder erstellen",
                                style = MaterialTheme.typography.titleLarge,
                                maxLines = 1
                            )
                            Text(
                                if (selectedGym == null) "Schritt 1: Gym wählen" else "Schritt 2: Spraywall wählen",
                                style = MaterialTheme.typography.titleSmall,
                                    color = Color.White.copy(alpha = 0.85f)

                            )
                        }
                    }
                )

            },
            ) { padding ->

            // unterhalb AppBar
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {

            when {
                isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                error != null -> Text(error ?: "", Modifier.align(Alignment.Center))

                // Gym Liste anzeigen, solange kein Gym gewählt ist
                selectedGym == null -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(gyms) { gym ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        // nachdem gym ausgewählt  zugehörige Spraywalls laden
                                        selectedGym = gym

                                        scope.launch {
                                            isLoading = true
                                            try {
                                                val res = RetrofitInstance
                                                    .getSpraywallApi(context)
                                                    .getSpraywallsByGym(gym.id)
                                                if (res.isSuccessful) {
                                                    spraywalls = res.body() ?: emptyList()
                                                } else {
                                                    error =
                                                        "Fehler beim Laden der Spraywalls (${res.code()})"
                                                }
                                            } catch (t: Throwable) {
                                                error = t.localizedMessage
                                            } finally {
                                                isLoading = false
                                            }
                                        }
                                    },
                                elevation = CardDefaults.cardElevation(6.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFFFFFFFF), // Weiß
                                    contentColor = Color(0xFF000000)    // Schwarz
                                )
                            ) {
                                Column(Modifier.padding(16.dp)) {
                                    Text(gym.name, style = MaterialTheme.typography.titleMedium)
                                    if (gym.location.isNotBlank()) {
                                        Text(
                                            gym.location,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                            }
                        }
                    }
                }



                !isLoading && spraywalls.isEmpty() -> {
                    EmptySpraywallsInPicker(
                    )
                }

                // Spraywalls anzeigen
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(spraywalls) { wall ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        // Navigiert in den Create-Screen;
                                        navController.navigate(
                                            "create_boulder/${wall.id}?imageUri=&mode=create&boulderId=&fromPicker=true"
                                        )
                                    },
                                elevation = CardDefaults.cardElevation(6.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFFFFFFFF), // Weiß
                                    contentColor = Color(0xFF000000)    // Schwarz
                                )
                            ) {
                                Column(Modifier.padding(16.dp).heightIn(min = 40.dp)) {
                                    Text(wall.name, style = MaterialTheme.typography.titleMedium)
                                    if (wall.description.isNotBlank()) {
                                        Text(
                                            wall.description,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }else {
                                        //hält die Höhe konstant
                                        Spacer(Modifier.height(18.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        }
    }
}



@Composable
private fun EmptySpraywallsInPicker(
) {


    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                imageVector = Icons.Outlined.ImageSearch,
                contentDescription = null,
                tint =  colorResource(R.color.button_normal_dark),
                modifier = Modifier.size(72.dp)
            )
            Text(
                "Keine Spraywalls gefunden",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
            )
            Text("Es wurden noch keine Spraywalls für dieses Gym erstellt.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
        }
    }
}
