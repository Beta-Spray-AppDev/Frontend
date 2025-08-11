package com.example.sprayconnectapp.ui.screens.BoulderView

import androidx.compose.runtime.Composable

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack

import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NavigateBefore
import androidx.compose.material.icons.filled.NavigateNext

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Size
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.sprayconnectapp.data.dto.HoldType
import com.example.sprayconnectapp.ui.screens.BoulderList.BoulderListViewmodel
import com.example.sprayconnectapp.util.getTokenFromPrefs
import com.example.sprayconnectapp.util.getUserIdFromToken

import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar

import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.text.font.FontWeight
import java.text.DateFormat
import java.util.Date


import com.example.sprayconnectapp.ui.screens.Profile.ProfileViewModel



import kotlin.math.roundToInt



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewBoulderScreen(
    navController: NavController,
    boulderId: String,
    spraywallId: String,
    imageUri: String,
    onBack: () -> Unit,
    viewModel: CreateBoulderViewModel = viewModel()
) {

    // previous entry - BoulderList ODER Profile
    val prevEntry = navController.previousBackStackEntry


    //Falls man vom BoulderListScreen kommt
    val listVm: BoulderListViewmodel? = prevEntry?.let { viewModel(it) }
    val gymList = listVm?.boulders?.value ?: emptyList()


    //Falls man vom Profil kommt
    val profileVm: ProfileViewModel? = prevEntry?.let { viewModel(it) }

    val myListState = if (profileVm != null) profileVm.myBoulders.collectAsState() else null
    val myList = myListState?.value ?: emptyList()



    val list = myList.ifEmpty { gymList }


    //Ui-State holen
    val context = LocalContext.current
    val uiState by viewModel.uiState
    val boulder = uiState.boulder

    //Prev & Next berechnen
    val currentId = boulder?.id
    val idx = list.indexOfFirst { it.id == currentId }
    val prevId = if (idx > 0) list[idx - 1].id else null
    val nextId = if (idx >= 0 && idx + 1 < list.size) list[idx + 1].id else null


    // Sate für Dialoge
    var showTickDialog by remember { mutableStateOf(false) }
    var showInfo by remember { mutableStateOf(false) }






    // Boulder laden
    LaunchedEffect(boulderId) {
        viewModel.loadBoulder(context, boulderId)
    }

    // Bildgröße ermitteln für korrekte Aspect Ratio
    var imgW by remember { mutableStateOf(1) }
    var imgH by remember { mutableStateOf(1) }
    var laidOut by remember { mutableStateOf(IntSize.Zero) }



    LaunchedEffect(imageUri) {
        if (imageUri.isBlank()) return@LaunchedEffect
        val uri = imageUri.toUri()
        if(uri.scheme == "content" || uri.scheme == "file"){
            val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(imageUri.toUri())?.use {
                BitmapFactory.decodeStream(it, null, opts)
            }
            imgW = if (opts.outWidth > 0) opts.outWidth else 1
            imgH = if (opts.outHeight > 0) opts.outHeight else 1
        }

    }

    val aspect = remember(imgW, imgH) { imgW.toFloat() / imgH.toFloat() }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                title = {Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Boulder: ${boulder?.name ?: "/"}",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Text(
                        text = "set by: ${boulder?.createdByUsername ?: "Unbekannter Setter"}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                    )
                }},

                //Zurück Button
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.padding(start = 8.dp)) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Zurück", modifier = Modifier.size(28.dp))
                    }
                },
                // Info Button
                actions = {
                    IconButton(onClick = { showInfo = true }, modifier = Modifier.padding(end = 8.dp)) {
                        Icon(Icons.Default.Info, contentDescription = "Info", modifier = Modifier.size(28.dp))
                    }
                }
            )
        },
        // FAB nur anzeigen, wenn aktueller User der Setter ist
        floatingActionButton = {
            val token = getTokenFromPrefs(context)
            val currentUserId = token?.let { getUserIdFromToken(it) }

            if ( boulder?.createdBy == currentUserId) {
                FloatingActionButton(
                    onClick = {
                        val encodedUri = Uri.encode(imageUri)
                        navController.navigate(
                            "create_boulder/$spraywallId?imageUri=$encodedUri&mode=edit&boulderId=$boulderId"
                        )
                    }
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Bearbeiten")
                }
            }
        },
        bottomBar = {

            BottomAppBar(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                tonalElevation = 4.dp,
                contentPadding = PaddingValues(horizontal = 12.dp)   ) {

                val iconSize = 35.dp

                //Prev Button
                IconButton(enabled = prevId != null, onClick = {
                    prevId?.let { navController.navigate("view_boulder/$it/$spraywallId/${Uri.encode(imageUri)}"){
                        launchSingleTop = true
                    } }
                }) {
                    Icon(Icons.Default.NavigateBefore, contentDescription = "Vorheriger Boulder", modifier = Modifier.size(iconSize))
                }

                Spacer(Modifier.weight(1f))


                //Boulder eintragen Button
                IconButton(
                    enabled = boulder?.id != null,
                    onClick = { showTickDialog = true }
                ) {
                    Icon(Icons.Default.Done, contentDescription = "Eintragen", modifier = Modifier.size(iconSize))
                }



                Spacer(Modifier.weight(1f))


                // Next Button
                IconButton(enabled = nextId != null, onClick = {
                    nextId?.let { navController.navigate("view_boulder/$it/$spraywallId/${Uri.encode(imageUri)}"){
                        launchSingleTop = true
                    } }
                }) {
                    Icon(
                        Icons.Default.NavigateNext,
                        contentDescription = "Nächster Boulder",
                        modifier = Modifier.size(iconSize)
                    )                }
            }
        }



    ) { padding ->
        val scale = remember { mutableStateOf(1f) }
        val pan = remember { mutableStateOf(Offset.Zero) }
        val tfState = rememberTransformableState { zoom, offset, _ ->
            scale.value = (scale.value * zoom).coerceIn(1f, 4f)
            pan.value += offset
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .transformable(tfState)
        ) {
            Box(
                modifier = Modifier
                    .graphicsLayer {
                        transformOrigin = TransformOrigin.Center
                        scaleX = scale.value
                        scaleY = scale.value
                        translationX = pan.value.x
                        translationY = pan.value.y
                    }
                    .align(Alignment.Center)
            ) {
                Box(
                    modifier = Modifier
                        .aspectRatio(aspect)
                        .fillMaxHeight()
                        .onGloballyPositioned { laidOut = it.size }
                ) {
                    if (imageUri.isNotBlank()) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(imageUri)
                                .size(Size.ORIGINAL)
                                .build(),
                            contentDescription = "Boulder",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        Text(
                            "Kein Bild verfügbar",
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }

                    // Holds aus ViewModel zeichnen
                    uiState.boulder?.holds?.forEach { hold ->
                        val color = HoldType.valueOf(hold.type).color
                        val posX = hold.x * laidOut.width
                        val posY = hold.y * laidOut.height

                        Box(
                            modifier = Modifier
                                .offset { IntOffset(posX.roundToInt(), posY.roundToInt()) }
                                .size(32.dp)
                                .drawBehind {
                                    drawCircle(color = Color.White, style = Stroke(6.dp.toPx()))
                                    drawCircle(color = color, style = Stroke(3.dp.toPx()))
                                }
                        )


                    }
                }
            }
        }
    }

    // Info-Dialog
    if (showInfo) {
        val created = formatDate(uiState.boulder?.createdAt)
        val updated = formatDate(uiState.boulder?.lastUpdated)
        AlertDialog(
            onDismissRequest = { showInfo = false },
            confirmButton = { TextButton(onClick = { showInfo = false }) { Text("OK") } },
            title = { Text("Boulder - Info") },
            text = {
                Column {
                    Text("Erstellt von: ${uiState.boulder?.createdByUsername ?: uiState.boulder?.createdBy?.take(8) ?: "-"}")
                    Text("Erstellt am: $created")
                    Text("Zuletzt aktualisiert: $updated")
                }
            }
        )
    }


    // Tick-Bestätigungsdialog
    if (showTickDialog) {
        val title = "Boulder eintragen?"
        val name = boulder?.name ?: "diesen Boulder"
        AlertDialog(
            onDismissRequest = { showTickDialog = false },
            title = { Text(title) },
            text  = { Text("Möchtest du „$name“ wirklich eintragen?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showTickDialog = false
                        boulder?.id?.let { viewModel.tickBoulder(context, it) }
                    }
                ) { Text("Ja, eintragen") }
            },
            dismissButton = {
                TextButton(onClick = { showTickDialog = false }) { Text("Abbrechen") }
            }
        )
    }





}

// Hilfsfunktion für Datum
private fun formatDate(ms: Long?): String {
    if (ms == null) return "-"
    val df = DateFormat.getDateInstance()
    return df.format(Date(ms))
}














