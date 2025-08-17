package com.example.sprayconnectapp.ui.screens.BoulderView

import android.graphics.BitmapFactory
import android.media.ExifInterface
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import com.example.sprayconnectapp.R
import androidx.compose.ui.res.colorResource

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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.NavigateBefore
import androidx.compose.material.icons.filled.NavigateNext
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType

import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.boundsInWindow
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
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalDensity



// --- Modus: Erstellen vs. Bearbeiten ---
sealed interface BoulderScreenMode {
    data object Create : BoulderScreenMode
    data class Edit(val boulderId: String) : BoulderScreenMode
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateBoulderScreen(
    spraywallId: String,
    imageUri: String?,
    mode: BoulderScreenMode = BoulderScreenMode.Create,
    viewModel: CreateBoulderViewModel = viewModel(),
    onSave: () -> Unit = {},
    onBack: () -> Unit = {},
    fromPicker: Boolean = false

) {

    // lokaler State für das Bild
    var resolvedImageUri by remember { mutableStateOf(imageUri ?: "") }
    var loadingImage by remember { mutableStateOf(false) }
    var imageError by remember { mutableStateOf<String?>(null) }

    val uiState by viewModel.uiState
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current

    val density = LocalDensity.current
    val markerSizeDp = 32.dp
    val markerRadiusPx = with(density) { (markerSizeDp / 2).toPx() }



    var showDialog by remember { mutableStateOf(false) }
    var boulderName by remember { mutableStateOf("") }
    var boulderDifficulty by remember { mutableStateOf("3") }
    var showTrash by remember { mutableStateOf(false) }
    var trashBounds by remember { mutableStateOf<androidx.compose.ui.geometry.Rect?>(null) }
    var overTrash by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var triedSave by remember { mutableStateOf(false) }
    val isNameValid = boulderName.trim().isNotEmpty()



    // Farbverlauf-Hintergrund
    val screenBg = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF53535B),
            Color(0xFF767981),
            Color(0xFFA8ABB2)
        )
    )


    var isPointerDownOnHold by remember { mutableStateOf(false) }

    // Bildgröße lesen, um die Aspect Ratio korrekt zu setzen
    var imgW by remember(resolvedImageUri) { mutableStateOf(1) }
    var imgH by remember(resolvedImageUri) { mutableStateOf(1) }

    // Prefill des Dialogs, sobald Daten da sind
    LaunchedEffect(uiState.boulder) {
        boulderName = uiState.boulder?.name.orEmpty()
        boulderDifficulty = uiState.boulder?.difficulty.orEmpty().ifEmpty { "3" }
    }

    // Falls im Edit-Modus dann Boulder-Daten laden
    LaunchedEffect(mode) {
        if (mode is BoulderScreenMode.Edit) {
            viewModel.loadBoulder(context, mode.boulderId)
        }
    }

    // Wenn kein imageUri mitkommt: von der Spraywall holen
    LaunchedEffect(spraywallId, imageUri) {
        if (resolvedImageUri.isBlank()) {
            try {
                loadingImage = true
                val api = com.example.sprayconnectapp.network.RetrofitInstance.getSpraywallApi(context)
                val res = api.getSpraywallById(java.util.UUID.fromString(spraywallId))
                if (res.isSuccessful) {
                    resolvedImageUri = res.body()?.photoUrl.orEmpty()
                } else {
                    imageError = "Konnte Spraywall nicht laden (${res.code()})"
                }
            } catch (t: Throwable) {
                imageError = t.localizedMessage
            } finally {
                loadingImage = false
            }
        }
    }


    // Bildmaße lesen
    // Bildmaße lesen (EXIF-beachtet)
    LaunchedEffect(resolvedImageUri) {
        if (resolvedImageUri.isBlank()) return@LaunchedEffect
        val u = resolvedImageUri.lowercase()
        val isRemote = u.startsWith("http://") || u.startsWith("https://")

        // Remote: keine EXIF-Lesung über ContentResolver
        if(isRemote){
            imgW = 1
            imgH = 1
            return@LaunchedEffect
        }
        val (w, h) = readImageSizeRespectingExif(context, resolvedImageUri.toUri())
        imgW = w
        imgH = h
    }


    val aspect = remember(imgW, imgH) { imgW.toFloat() / imgH.toFloat() }


    val BarColor = colorResource(id = R.color.hold_type_bar)


    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = BarColor,
                    scrolledContainerColor = BarColor,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White

                ),
                title = {
                    Text(if (mode is BoulderScreenMode.Edit) "Boulder bearbeiten" else "Boulder erstellen")
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Zurück")
                    }
                },
                actions = {
                    //Lösch Button nur im Edit Modus
                    if (mode is BoulderScreenMode.Edit) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Boulder löschen")
                        }
                    }

                    //Speichern Button
                    IconButton(onClick = { showDialog = true }) {
                        Icon(Icons.Default.Check, contentDescription = "Speichern")
                    }
                },

                )

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
                .background(screenBg)
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

                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
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
                        if (resolvedImageUri.isNotBlank()) {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(resolvedImageUri)
                                    .size(Size.ORIGINAL)
                                    .build(),
                                contentDescription = "Spraywall",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit,
                                onSuccess = { s ->
                                    if (resolvedImageUri.startsWith("http", ignoreCase = true)) {
                                        val d = s.result.drawable
                                        imgW = d.intrinsicWidth.coerceAtLeast(1)
                                        imgH = d.intrinsicHeight.coerceAtLeast(1)
                                    }
                                }
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

                            var holdCoords by remember { mutableStateOf<LayoutCoordinates?>(null) }

                            Box(
                                modifier = Modifier
                                    .offset {
                                        IntOffset(
                                            (baseX - markerRadiusPx).roundToInt(),
                                            (baseY - markerRadiusPx).roundToInt()
                                        )
                                    }
                                    .size(markerSizeDp)
                                    .onGloballyPositioned { coords -> holdCoords = coords }
                                    .pointerInput(hold.id, laidOut, scale.value) {
                                        awaitEachGesture {
                                            val down = awaitFirstDown(requireUnconsumed = false)
                                            showTrash = true
                                            overTrash = false

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
                                                    val change =
                                                        event.changes.firstOrNull { it.id == down.id }
                                                            ?: event.changes.first()

                                                    val deltaPx = change.position - lastPos
                                                    lastPos = change.position
                                                    change.consume()

                                                    if (laidOut.width != 0 && laidOut.height != 0) {
                                                        val dx =
                                                            deltaPx.x / (laidOut.width * scale.value)
                                                        val dy =
                                                            deltaPx.y / (laidOut.height * scale.value)
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

                                                    val fingerInWindow =
                                                        holdCoords?.localToWindow(change.position)
                                                    overTrash = trashBounds?.contains(
                                                        fingerInWindow ?: Offset.Zero
                                                    ) == true

                                                    if (!change.pressed) break
                                                }
                                            } finally {
                                                val fingerUpInWindow =
                                                    holdCoords?.localToWindow(lastPos)
                                                if (trashBounds?.contains(
                                                        fingerUpInWindow ?: Offset.Zero
                                                    ) == true
                                                ) {
                                                    viewModel.removeHold(hold.id)
                                                }
                                                showTrash = false
                                                overTrash = false
                                                isPointerDownOnHold = false
                                            }
                                        }
                                    }
                                    .drawBehind {

                                        // Außenring (Selection)
                                        drawCircle(
                                            color = if (isSelected) Color.Gray else Color.White,
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
            HoldTypePicker(
                types = HoldType.entries,
                selected = uiState.selectedType,
                onSelect = { viewModel.selectHoldType(it) },
                modifier = Modifier.align(Alignment.BottomCenter)
            )


            //TRASH DOCK OVERLAY
            if (showTrash) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(start = 24.dp, top = 24.dp)
                        .size(if (overTrash) 72.dp else 64.dp)
                        .background(Color(0xFF2B2B2B), CircleShape)
                        .border(1.dp, Color.White.copy(0.16f), CircleShape)
                        .onGloballyPositioned { coords ->
                            trashBounds = coords.boundsInWindow()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "In den Papierkorb",
                        tint = if (overTrash) Color(0xFFFF6B6B) else Color.White.copy(0.9f)
                    )
                }
            }

        }

        // Speichern-Dialog
        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                confirmButton = {
                    TextButton( onClick = {
                        Log.d(
                            "BoulderUpdate",
                            "Confirm clicked. mode=$mode  name=$boulderName  diff=$boulderDifficulty"
                        )

                        // falls User keinen Namen eingegeben hat
                        if (boulderName.isBlank()) {
                            triedSave = true              // Fehler anzeigen
                            return@TextButton
                        }

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
                        if (fromPicker) { // Nur wenn wir vom Picker kommen
                            onBack()
                        }
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
                            label = { Text("Name") },
                            isError = triedSave && !isNameValid,
                            supportingText = {
                                if (triedSave && !isNameValid) Text("Bitte einen Namen eingeben")
                            }
                        )
                        Spacer(Modifier.height(8.dp))

                        // Schwierigkeitsauswahl als Stepper
                        val fbGrades = listOf(
                            "3", "4", "5A", "5B", "5C",
                            "6A", "6A+", "6B", "6B+", "6C", "6C+",
                            "7A", "7A+", "7B", "7B+", "7C", "7C+",
                            "8A", "8A+", "8B", "8B+", "8C", "8C+", "9A"
                        )


                        DifficultyStepper(
                            options = fbGrades,
                            value = boulderDifficulty.ifEmpty { fbGrades.first() },
                            onValueChange = { boulderDifficulty = it }
                        )
                    }
                }
            )
        }

        if (showDeleteDialog && mode is BoulderScreenMode.Edit) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Boulder löschen?") },
                text = { Text("Dieser Vorgang kann nicht rückgängig gemacht werden.") },
                confirmButton = {
                    TextButton(onClick = {
                        showDeleteDialog = false


                            viewModel.deleteBoulder(
                                context = context,
                                boulderId = mode.boulderId
                            ) {
                                Toast.makeText(context, "Boulder gelöscht", Toast.LENGTH_SHORT).show()
                                onBack()   // zurück von Edit/View
                                onBack()   // weiter zurück zur Liste
                            }

                    }) { Text("Löschen") }
                }
                ,
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) { Text("Abbrechen") }
                }
            )
        }

    }


}

private fun readImageSizeRespectingExif(
    context: android.content.Context,
    uri: android.net.Uri
): Pair<Int, Int> {
    // Rohmaße (ignoriert Rotation)
    val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    context.contentResolver.openInputStream(uri)?.use { input ->
        BitmapFactory.decodeStream(input, null, opts)
    }
    var w = if (opts.outWidth > 0) opts.outWidth else 1
    var h = if (opts.outHeight > 0) opts.outHeight else 1

    // EXIF-Orientation lesen
    val orientation = context.contentResolver.openInputStream(uri)?.use { input ->
        ExifInterface(input).getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL
        )
    } ?: ExifInterface.ORIENTATION_NORMAL

    // 90°/270° → w/h tauschen
    val needsSwap = orientation == ExifInterface.ORIENTATION_ROTATE_90 ||
            orientation == ExifInterface.ORIENTATION_TRANSPOSE ||
            orientation == ExifInterface.ORIENTATION_TRANSVERSE ||
            orientation == ExifInterface.ORIENTATION_ROTATE_270

    if (needsSwap) {
        val tmp = w; w = h; h = tmp
    }
    return w to h
}



// --- kleine Extension für null-sichere Strings ---
private fun String?.orElseEmpty() = this ?: ""




// Schwierigkeitsauswahl
@Composable
fun DifficultyStepper(
    label: String = "Schwierigkeit",
    options: List<String>,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // falls value noch leer/ungültig - erstes Element
    val currentIndex = options.indexOf(value).let { if (it >= 0) it else 0 }

    Column(modifier) {
        Text(label)
        Spacer(Modifier.height(4.dp))
        Row(
            modifier = Modifier
                .height(56.dp)
                .fillMaxWidth()
                .border(1.dp, Color(0x33000000), shape = CircleShape)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = {
                    val newIndex = (currentIndex - 1).coerceAtLeast(0)
                    onValueChange(options[newIndex])
                },
                enabled = currentIndex > 0
            ) {
                Icon(Icons.Default.NavigateBefore, contentDescription = "Niedriger")
            }

            Text(
                text = options.getOrElse(currentIndex) { options.firstOrNull().orEmpty() },
                style = androidx.compose.material3.MaterialTheme.typography.titleMedium
            )

            IconButton(
                onClick = {
                    val newIndex = (currentIndex + 1).coerceAtMost(options.lastIndex)
                    onValueChange(options[newIndex])
                },
                enabled = currentIndex < options.lastIndex
            ) {
                Icon(Icons.Default.NavigateNext, contentDescription = "Höher")
            }
        }
    }
}

