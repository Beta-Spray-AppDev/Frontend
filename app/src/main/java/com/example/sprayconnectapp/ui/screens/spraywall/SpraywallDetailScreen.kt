package com.example.sprayconnectapp.ui.screens.spraywall

// Android / System
import android.app.DownloadManager
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat

// Compose
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

// Navigation + Lifecycle
import androidx.navigation.NavController
import androidx.lifecycle.viewmodel.compose.viewModel

// Coil
import coil.compose.AsyncImage

// Dein Model
import com.example.sprayconnectapp.data.dto.SpraywallDTO

// Utils (Download)

import com.example.sprayconnectapp.util.buildDownloadUrlFromPreview
import com.example.sprayconnectapp.util.downloadDirectToPrivate
import com.example.sprayconnectapp.util.getPrivateImageFileByName
import com.example.sprayconnectapp.util.localOutputNameFromPreview
import kotlinx.coroutines.launch
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

// LaunchedEffect
import androidx.compose.runtime.LaunchedEffect

// Icons
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import com.example.sprayconnectapp.ui.screens.BottomNavigationBar


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpraywallDetailScreen(
    navController: NavController,
    gymId: String,
    gymName: String,
    viewModel: SpraywallViewModel = viewModel()
) {

    val spraywalls by viewModel.spraywalls
    val isLoading by viewModel.isLoading
    val errorMessage by viewModel.errorMessage
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val DL_TAG = "SprayDL"


    fun startDownloadAndOpen(s: SpraywallDTO) {
        val preview = s.photoUrl.trim()
        val token = Regex("/s/([^/]+)/").find(preview)?.groupValues?.get(1)
        if (token.isNullOrBlank()) {
            Toast.makeText(context, "Kein gültiger Token in der Bild-URL.", Toast.LENGTH_SHORT).show()
            return
        }

        val downloadUrl = buildDownloadUrlFromPreview(preview) ?: run {
            Toast.makeText(context, "Konnte Download-URL nicht bauen.", Toast.LENGTH_SHORT).show()
            return
        }

        val outName = localOutputNameFromPreview(preview, token)
        val file = getPrivateImageFileByName(context, outName)

        fun navigateToBoulderList(localUri: Uri?) {
            val encodedImage = localUri?.let { Uri.encode(it.toString()) }
            val queryPart = encodedImage?.let { "?imageUri=$it" } ?: ""
            val spraywallId = s.id?.toString() ?: ""
            val spraywallName = Uri.encode(s.name)


            navController.navigate("boulders/$spraywallId/$spraywallName$queryPart")
        }



        if (file.exists()) {
            // Lokale Datei schon da → gleich weiter
            navigateToBoulderList(Uri.fromFile(file))
        } else {
            // Download im Hintergrund starten, danach weiter
            val referer = "https://leitln.at/maltacloud/index.php/s/$token/preview"
            scope.launch {
                try {
                    val localUri = downloadDirectToPrivate(context, downloadUrl, outName, referer)
                    navigateToBoulderList(localUri)
                } catch (e: Exception) {
                    Log.e(DL_TAG, "Download fehlgeschlagen: ${e.message}", e)
                    Toast.makeText(context, "Download fehlgeschlagen: ${e.message}", Toast.LENGTH_SHORT).show()
                    // Auch bei Fehler weitergehen, aber ohne Bild
                    navigateToBoulderList(null)
                }
            }
        }
    }


    LaunchedEffect(gymId) {
        viewModel.loadSpraywalls(context, gymId)
    }


    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = gymName) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Zurück")
                    }
                }
            )
        },
        bottomBar = {BottomNavigationBar(navController) }


    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            when {
                isLoading -> CircularProgressIndicator()
                errorMessage != null -> Text(
                    text = errorMessage ?: "Unbekannter Fehler",
                    color = MaterialTheme.colorScheme.error
                )
                spraywalls.isEmpty() -> Text("Keine Spraywalls gefunden.")
                else -> {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(spraywalls) { spraywall ->
                            SpraywallCard(
                                spraywall = spraywall,
                                onClick = { startDownloadAndOpen(spraywall) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SpraywallCard(
    spraywall: SpraywallDTO,
    onClick: () -> Unit
) {
    val cleanUrl = spraywall.photoUrl.trim() // Preview-URL
    Log.d("SpraywallCard", "URL geladen: [$cleanUrl]")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = spraywall.name, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = spraywall.description, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(8.dp))

            AsyncImage(
                model = cleanUrl,
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


