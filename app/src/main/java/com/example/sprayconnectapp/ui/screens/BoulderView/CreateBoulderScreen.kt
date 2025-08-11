package com.example.sprayconnectapp.ui.screens.BoulderView

import android.graphics.BitmapFactory
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer

import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Size
import com.example.sprayconnectapp.data.dto.HoldType
import kotlin.math.roundToInt

// --- Modus: Erstellen vs. Bearbeiten ---
sealed interface BoulderScreenMode {
    data object Create : BoulderScreenMode
    data class Edit(val boulderId: String) : BoulderScreenMode
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateBoulderScreen(
    spraywallId: String,
    imageUri: String,
    mode: BoulderScreenMode = BoulderScreenMode.Create,
    viewModel: CreateBoulderViewModel = viewModel(),
    onSave: () -> Unit = {},
    onBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState
    val context = LocalContext.current

    var showDialog by remember { mutableStateOf(false) }
    var boulderName by remember { mutableStateOf("") }
    var boulderDifficulty by remember { mutableStateOf("") }

    var isPointerDownOnHold by remember { mutableStateOf(false) }

    // Bildgröße lesen, um die Aspect Ratio korrekt zu setzen
    var imgW by remember(imageUri) { mutableStateOf(1) }
    var imgH by remember(imageUri) { mutableStateOf(1) }

    // Prefill des Dialogs, sobald Daten da sind
    LaunchedEffect(uiState.boulder) {
        boulderName = uiState.boulder?.name.orEmpty()
        boulderDifficulty = uiState.boulder?.difficulty.orEmpty()
    }

    LaunchedEffect(mode) {
        if (mode is BoulderScreenMode.Edit) {
            viewModel.loadBoulder(context, mode.boulderId)
        }
    }


    // Bildmaße lesen
    LaunchedEffect(imageUri) {
        if (imageUri.isBlank()) return@LaunchedEffect
        val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(imageUri.toUri())?.use { input ->
            BitmapFactory.decodeStream(input, null, opts)
        }
        imgW = if (opts.outWidth > 0) opts.outWidth else 1
        imgH = if (opts.outHeight > 0) opts.outHeight else 1
    }

    val aspect = remember(imgW, imgH) { imgW.toFloat() / imgH.toFloat() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (mode is BoulderScreenMode.Edit) "Boulder bearbeiten"
                        else "Boulder erstellen"
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Zurück")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = if (mode is BoulderScreenMode.Edit)
                        "Änderungen speichern" else "Speichern"
                )
            }
        }
    ) { padding ->

        var laidOut by remember { mutableStateOf(IntSize.Zero) }
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
                .transformable(tfState, enabled = !isPointerDownOnHold)
        ) {
            // Gesten-Ebene für Long-Press zum Hinzufügen neuer Holds
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(scale.value, pan.value, laidOut) {
                        detectTapGestures(
                            onLongPress = { tapRoot ->
                                if (laidOut == IntSize.Zero) return@detectTapGestures

                                val parentW = size.width.toFloat()
                                val parentH = size.height.toFloat()
                                val unscaledW = laidOut.width.toFloat()
                                val unscaledH = laidOut.height.toFloat()
                                val scaledW = unscaledW * scale.value
                                val scaledH = unscaledH * scale.value

                                val center = Offset(parentW / 2f, parentH / 2f) + pan.value
                                val topLeft = center - Offset(scaledW / 2f, scaledH / 2f)
                                val rel = (tapRoot - topLeft)

                                if (rel.x !in 0f..scaledW || rel.y !in 0f..scaledH) return@detectTapGestures

                                val unscaled = rel / scale.value
                                val nx = unscaled.x / unscaledW
                                val ny = unscaled.y / unscaledH

                                viewModel.addHoldNorm(nx, ny)
                            }
                        )
                    }
            ) {
                // Transformierter Container (Zoom/Pan)
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
                    // Bildrahmen mit korrekter Aspect Ratio
                    Box(
                        modifier = Modifier
                            .aspectRatio(aspect)
                            .fillMaxHeight()
                            .onGloballyPositioned { laidOut = it.size }
                    ) {
                        if (imageUri.isNotBlank()) {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(imageUri.toUri())
                                    .size(Size.ORIGINAL)
                                    .build(),
                                contentDescription = "Spraywall",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                        } else {
                            Text(
                                "Kein Bild verfügbar",
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }

                        // Holds zeichnen + Draggen
                        uiState.holds.forEach { hold ->
                            val color = HoldType.valueOf(hold.type).color
                            val isSelected = uiState.selectedHoldId == hold.id

                            val baseX = hold.x * laidOut.width
                            val baseY = hold.y * laidOut.height

                            Box(
                                modifier = Modifier
                                    .offset { IntOffset(baseX.roundToInt(), baseY.roundToInt()) }
                                    .size(32.dp)
                                    .pointerInput(hold.id, laidOut, scale.value) {
                                        awaitEachGesture {
                                            val down = awaitFirstDown(requireUnconsumed = false)

                                            val startHold = uiState.holds.first { it.id == hold.id }
                                            var currentNorm = Offset(startHold.x, startHold.y)

                                            if (uiState.selectedHoldId != hold.id) {
                                                viewModel.selectHold(hold.id)
                                            }

                                            isPointerDownOnHold = true
                                            down.consume()
                                            var lastPos = down.position

                                            try {
                                                while (true) {
                                                    val event = awaitPointerEvent()
                                                    val change = event.changes.firstOrNull { it.id == down.id }
                                                        ?: event.changes.first()

                                                    val deltaPx = change.position - lastPos
                                                    lastPos = change.position
                                                    change.consume()

                                                    if (laidOut.width != 0 && laidOut.height != 0) {
                                                        val dx = deltaPx.x / (laidOut.width * scale.value)
                                                        val dy = deltaPx.y / (laidOut.height * scale.value)
                                                        currentNorm = Offset(
                                                            (currentNorm.x + dx).coerceIn(0f, 1f),
                                                            (currentNorm.y + dy).coerceIn(0f, 1f)
                                                        )
                                                        viewModel.updateHoldPosition(
                                                            hold.id,
                                                            currentNorm.x,
                                                            currentNorm.y
                                                        )
                                                    }

                                                    if (!change.pressed) break
                                                }
                                            } finally {
                                                isPointerDownOnHold = false
                                            }
                                        }
                                    }
                                    .drawBehind {
                                        // Außenring (Selection)
                                        drawCircle(
                                            color = if (isSelected) Color.Yellow else Color.White,
                                            style = Stroke(6.dp.toPx())
                                        )
                                        // Innenring (Hold-Farbe)
                                        drawCircle(color, style = Stroke(3.dp.toPx()))
                                    }
                            )
                        }
                    }
                }
            }

            // Farb-Auswahl
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                HoldType.entries.forEach { type ->
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .background(type.color, CircleShape)
                            .border(
                                width = if (type == uiState.selectedType) 4.dp else 2.dp,
                                color = if (type == uiState.selectedType) Color.Black else Color.LightGray,
                                shape = CircleShape
                            )
                            .clickable { viewModel.selectHoldType(type) }
                    )
                }
            }
        }

        // Speichern-Dialog
        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                confirmButton = {
                    TextButton(onClick = {
                        Log.d("BoulderUpdate","Confirm clicked. mode=$mode  name=$boulderName  diff=$boulderDifficulty")

                        if (mode is BoulderScreenMode.Edit) {
                            viewModel.updateBoulder(
                                context = context,
                                name = boulderName,
                                difficulty = boulderDifficulty,
                                spraywallId = spraywallId,
                                boulderIdOverride = mode.boulderId
                            )
                            Toast
                                .makeText(context, "Boulder aktualisiert", Toast.LENGTH_SHORT)
                                .show()
                        } else {
                            viewModel.saveBoulder(
                                context = context,
                                name = boulderName,
                                difficulty = boulderDifficulty,
                                spraywallId = spraywallId
                            )
                            Toast
                                .makeText(context, "Boulder erstellt", Toast.LENGTH_SHORT)
                                .show()
                        }

                        showDialog = false
                        onSave()
                        onBack() // Einen Screen zurück
                    }) {
                        Text(if (mode is BoulderScreenMode.Edit) "Speichern" else "Anlegen")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) { Text("Abbrechen") }
                },
                title = {
                    Text(
                        if (mode is BoulderScreenMode.Edit) "Boulder speichern"
                        else "Boulder anlegen"
                    )
                },
                text = {
                    Column {
                        OutlinedTextField(
                            value = boulderName,
                            onValueChange = { boulderName = it },
                            label = { Text("Name") }
                        )
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = boulderDifficulty,
                            onValueChange = { boulderDifficulty = it },
                            label = { Text("Schwierigkeit") }
                        )
                    }
                }
            )
        }


    }
}

// --- kleine Extension für null-sichere Strings ---
private fun String?.orElseEmpty() = this ?: ""
