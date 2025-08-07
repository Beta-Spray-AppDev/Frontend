package com.example.sprayconnectapp.ui.screens.BoulderView

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PaintingStyle.Companion.Stroke
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sprayconnectapp.R
import com.example.sprayconnectapp.data.dto.HoldType


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateBoulderScreen(
    spraywallId: String,
    viewModel: CreateBoulderViewModel = viewModel(),
    onSave: () -> Unit = {},
    onBack: () -> Unit = {}

) {
    val uistate by viewModel.uiState
    val context = LocalContext.current



    var showDialog by remember { mutableStateOf(false) }
    var boulderName by remember { mutableStateOf("") }
    var boulderDifficulty by remember { mutableStateOf("") }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Boulder erstellen") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Zurück")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Check, contentDescription = "Speichern")
            }

        }
    ) { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Spraywall-Bild - später vom server laden momentan lokal
            // Vorbereitung
            val image = painterResource(id = R.drawable.spray1)
            val imageWidth = image.intrinsicSize.width
            val imageHeight = image.intrinsicSize.height

            var imageSize by remember { mutableStateOf<IntSize>(IntSize.Zero) }
            val scale = remember { mutableStateOf(1f) }
            val offset = remember { mutableStateOf(Offset.Zero) }

            val minScale = 1f
            val maxScale = 4f

            val transformState = rememberTransformableState { zoomChange, panChange, _ ->
                scale.value = (scale.value * zoomChange).coerceIn(minScale, maxScale)
                offset.value += panChange
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .transformable(state = transformState)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onLongPress = { offsetInBox ->
                                val imageOffset = (offsetInBox - offset.value) / scale.value
                                viewModel.addHold(imageOffset.x, imageOffset.y)
                            }
                        )
                    }
            ) {
                Box(
                    modifier = Modifier
                        .graphicsLayer {
                            scaleX = scale.value
                            scaleY = scale.value
                            translationX = offset.value.x
                            translationY = offset.value.y
                        }
                        .align(Alignment.Center)
                ) {
                    val aspectRatio = image.intrinsicSize.width / image.intrinsicSize.height

                    Box(
                        modifier = Modifier
                            .aspectRatio(aspectRatio)
                            .fillMaxHeight()
                            .onGloballyPositioned { imageSize = it.size }
                    ) {
                        Image(
                            painter = image,
                            contentDescription = "Spraywall",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize()
                        )

                        // Holds zeichnen (INNEN, damit sie mitskalieren!)
                        uistate.holds.forEach { hold ->
                            val color = HoldType.valueOf(hold.type).color

                            val scaleX = imageSize.width.toFloat() / imageWidth
                            val scaleY = imageSize.height.toFloat() / imageHeight

                            Box(
                                modifier = Modifier
                                    .offset {
                                        IntOffset(
                                            (hold.x * scaleX).toInt(),
                                            (hold.y * scaleY).toInt()
                                        )
                                    }
                                    .size(28.dp)
                                    .drawBehind {
                                        drawCircle(
                                            color = Color.White,
                                            style = Stroke(width = 6.dp.toPx())
                                        )
                                        drawCircle(
                                            color = color,
                                            style = Stroke(width = 3.dp.toPx())
                                        )
                                    }
                            )
                        }
                    }
                }
            }





            // Farb-Auswahl unten für Kreise
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
                            .background(color = type.color, shape = CircleShape)
                            .border(
                                width = if (type == uistate.selectedType) 4.dp else 2.dp,
                                color = if (type == uistate.selectedType) Color.Black else Color.LightGray,
                                shape = CircleShape
                            )
                            .clickable { viewModel.selectHoldType(type) }
                    )
                }
            }


            //Dialogfenster wenn User Boulder speichern will
            if (showDialog) {
                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    confirmButton = {
                        TextButton(onClick = {
                            viewModel.saveBoulder(
                                context = context,
                                name = boulderName,
                                difficulty = boulderDifficulty,
                                spraywallId = spraywallId
                            )
                            showDialog = false
                            onSave()
                        }) {
                            Text("Speichern")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDialog = false }) {
                            Text("Abbrechen")
                        }
                    },
                    title = { Text("Boulder speichern") },
                    text = {
                        Column {
                            OutlinedTextField(
                                value = boulderName,
                                onValueChange = { boulderName = it },
                                label = { Text("Name") }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
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
}