package com.example.sprayconnectapp.ui.screens.spraywall

// Jetpack Compose
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Navigation
import androidx.navigation.NavController

// Lifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

// Coil für Bilder
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow

// SpraywallDTO
import com.example.sprayconnectapp.data.dto.SpraywallDTO

// ViewModel & Context
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

// LaunchedEffect
import androidx.compose.runtime.LaunchedEffect

// Icons
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpraywallDetailScreen(
    navController: NavController,
    gymId: String,
    gymName: String,
    viewModel: SpraywallViewModel = viewModel()
) {
    val context = LocalContext.current
    val spraywalls by viewModel.spraywalls
    val isLoading by viewModel.isLoading
    val errorMessage by viewModel.errorMessage

    // Lade Daten bei Composable-Start
    LaunchedEffect(Unit) {
        viewModel.loadSpraywalls(context, gymId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = gymName) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Zurück")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator()
                }
                errorMessage != null -> {
                    Text(
                        text = errorMessage ?: "Unbekannter Fehler",
                        color = MaterialTheme.colorScheme.error
                    )
                }
                spraywalls.isEmpty() -> {
                    Text("Keine Spraywalls gefunden.")
                }
                else -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(spraywalls) { spraywall ->
                            SpraywallCard(spraywall)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SpraywallCard(spraywall: SpraywallDTO) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = spraywall.name, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = spraywall.description, style = MaterialTheme.typography.bodyMedium)

            Spacer(modifier = Modifier.height(8.dp))

            AsyncImage(
                model = spraywall.photoUrl,
                contentDescription = "Spraywall Bild",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
        }
    }
}

