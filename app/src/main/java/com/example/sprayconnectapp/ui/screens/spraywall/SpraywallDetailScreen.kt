package com.example.sprayconnectapp.ui.screens.spraywall

import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.sprayconnectapp.R
import com.example.sprayconnectapp.data.dto.SpraywallDTO
import com.example.sprayconnectapp.ui.screens.BottomNavigationBar
import com.example.sprayconnectapp.util.buildDownloadUrlFromPreview
import com.example.sprayconnectapp.util.downloadDirectToPrivate
import com.example.sprayconnectapp.util.getPrivateImageFileByName
import com.example.sprayconnectapp.util.localOutputNameFromPreview
import kotlinx.coroutines.launch

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
            navigateToBoulderList(Uri.fromFile(file))
        } else {
            val referer = "https://leitln.at/maltacloud/index.php/s/$token/preview"
            scope.launch {
                try {
                    val localUri = downloadDirectToPrivate(context, downloadUrl, outName, referer)
                    navigateToBoulderList(localUri)
                } catch (e: Exception) {
                    Log.e(DL_TAG, "Download fehlgeschlagen: ${e.message}", e)
                    Toast.makeText(context, "Download fehlgeschlagen: ${e.message}", Toast.LENGTH_SHORT).show()
                    navigateToBoulderList(null)
                }
            }
        }
    }

    LaunchedEffect(gymId) {
        viewModel.initRepository(context)
        viewModel.loadSpraywalls(context, gymId)
    }

    val encodedGymName = Uri.encode(gymName)
    val BarColor = colorResource(id = R.color.hold_type_bar)

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
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text(text = gymName) },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = BarColor,
                        scrolledContainerColor = BarColor,
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White,
                        actionIconContentColor = Color.White
                    ),
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Zurück")
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { navController.navigate("addSpraywall/$gymId/$encodedGymName") }
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Spraywall hinzufügen")
                        }
                    }
                )
            },
            bottomBar = { BottomNavigationBar(navController) }
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

                    spraywalls.isNotEmpty() -> {
                        errorMessage?.let {
                            Text(text = it, color = MaterialTheme.colorScheme.error)
                            Spacer(Modifier.height(8.dp))
                        }
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(spraywalls) { spraywall ->
                                SpraywallCard(
                                    spraywall = spraywall,
                                    onClick = { startDownloadAndOpen(spraywall) }
                                )
                            }
                        }
                    }

                    errorMessage != null -> {
                        Text(
                            text = errorMessage ?: "Unbekannter Fehler",
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    else -> {
                        Text("Keine Spraywalls gefunden.")
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
    val cleanUrl = spraywall.photoUrl.trim()
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
