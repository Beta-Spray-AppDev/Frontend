package com.example.sprayconnectapp.ui.screens.BoulderView

import androidx.compose.foundation.Image
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.*
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
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateBoulderScreen(
    spraywallId: String,
    viewModel: CreateBoulderViewModel = viewModel(),
    onSave: () -> Unit = {},
    onBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState
    val context = LocalContext.current

    var showDialog by remember { mutableStateOf(false) }
    var boulderName by remember { mutableStateOf("") }
    var boulderDifficulty by remember { mutableStateOf("") }

    // Sperrt das Parent-Transformable, solange ein Finger auf einem Hold liegt
    var isPointerDownOnHold by remember { mutableStateOf(false) }

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

        val painter = painterResource(id = R.drawable.spray1)
        val bmpW = painter.intrinsicSize.width
        val bmpH = painter.intrinsicSize.height

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
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    // Long-Press auf den Hintergrund zum Hold hinzufügen bleibt wie gehabt
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
                val aspect = bmpW / bmpH

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
                        Image(
                            painter = painter,
                            contentDescription = "Spraywall",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize()
                        )

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
                                                        viewModel.updateHoldPosition(hold.id, currentNorm.x, currentNorm.y)
                                                    }

                                                    if (!change.pressed) break
                                                }
                                            } finally {
                                                isPointerDownOnHold = false
                                            }
                                        }
                                    }

                                    .drawBehind {
                                        drawCircle(
                                            color = if (isSelected) Color.Yellow else Color.White,
                                            style = Stroke(6.dp.toPx())
                                        )
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

        // Save Dialog
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
                    }) { Text("Speichern") }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) { Text("Abbrechen") }
                },
                title = { Text("Boulder speichern") },
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
