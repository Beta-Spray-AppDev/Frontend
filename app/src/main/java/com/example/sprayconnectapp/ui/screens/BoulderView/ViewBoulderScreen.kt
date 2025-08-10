package com.example.sprayconnectapp.ui.screens.BoulderView

import androidx.compose.runtime.Composable

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import com.example.sprayconnectapp.data.dto.Hold
import com.example.sprayconnectapp.data.dto.HoldType
import kotlin.math.roundToInt


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewBoulderScreen(
    imageUri: String,
    boulderId: String,
    onBack: () -> Unit,
    viewModel: CreateBoulderViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState

    // Backend call starten
    LaunchedEffect(boulderId) {
        viewModel.loadBoulder(context, boulderId)
    }

    // Bildgröße ermitteln für korrekte Aspect Ratio
    var imgW by remember { mutableStateOf(1) }
    var imgH by remember { mutableStateOf(1) }
    var laidOut by remember { mutableStateOf(IntSize.Zero) }

    LaunchedEffect(imageUri) {
        if (imageUri.isBlank()) return@LaunchedEffect
        val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(Uri.parse(imageUri))?.use {
            BitmapFactory.decodeStream(it, null, opts)
        }
        imgW = if (opts.outWidth > 0) opts.outWidth else 1
        imgH = if (opts.outHeight > 0) opts.outHeight else 1
    }

    val aspect = remember(imgW, imgH) { imgW.toFloat() / imgH.toFloat() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.boulder?.name ?: "Boulder anzeigen") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Zurück")
                    }
                }
            )
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
                                .data(imageUri.toUri())
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
}

