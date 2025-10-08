package com.example.sprayconnectapp.ui.screens.BoulderView

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.BitmapFactory
import androidx.exifinterface.media.ExifInterface
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NavigateBefore
import androidx.compose.material.icons.filled.NavigateNext
import androidx.compose.material.icons.outlined.ImageSearch
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.input.pointer.PointerEventPass

import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.input.pointer.positionChange
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
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import androidx.core.content.edit
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.rememberAsyncImagePainter


// Modus: Erstellen und Bearbeiten
sealed interface BoulderScreenMode {
    data object Create : BoulderScreenMode
    data class Edit(val boulderId: String) : BoulderScreenMode
}

@SuppressLint("UseKtx")
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

    val MAX_NAME = 35
    val MAX_NOTE = 500

    // lokaler State für das Bild
    var resolvedImageUri by remember { mutableStateOf(imageUri ?: "") }
    var loadingImage by remember { mutableStateOf(false) }
    var imageError by remember { mutableStateOf<String?>(null) }

    val uiState by viewModel.uiState
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current
    var hasBuzzedOverTrash by remember { mutableStateOf(false) }


    // Markergröße/-radius für die Hold-Zeichnung
    val density = LocalDensity.current
    val markerSizeDp = 32.dp
    val markerRadiusPx = with(density) { (markerSizeDp / 2).toPx() }


    // Dialog-/Form-State
    var showDialog by remember { mutableStateOf(false) }
    var boulderName by remember { mutableStateOf("") }
    var boulderDifficulty by remember { mutableStateOf("3") }
    var setterNote by remember { mutableStateOf("") }
    var showTrash by remember { mutableStateOf(false) }
    var trashBounds by remember { mutableStateOf<androidx.compose.ui.geometry.Rect?>(null) }
    var overTrash by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var triedSave by remember { mutableStateOf(false) }
    val isNameValid = boulderName.trim().isNotEmpty()

    val conDtext = LocalContext.current
    var showOnboarding by remember {
        mutableStateOf(
            context.getSharedPreferences("prefs", Context.MODE_PRIVATE)
                .getBoolean("showOnboarding", true)
        )
    }







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
        setterNote = uiState.boulder?.setterNote.orEmpty()
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

    // Haptik für Trash-Zone: einmaliges Buzz beim Reindrücken, reset beim Rausgehen
    LaunchedEffect(showTrash, overTrash) {
        if (!showTrash) {
            // Drag zu Ende -> zurücksetzen
            hasBuzzedOverTrash = false
        } else if (overTrash && !hasBuzzedOverTrash) {
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            hasBuzzedOverTrash = true
        } else if (!overTrash) {
            // wieder raus -> erlaub nächstes Buzz, falls erneut rein
            hasBuzzedOverTrash = false
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


    // AppBar + Aktionen (Speichern/Löschen)
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
                    //Löschen Button nur im Edit Modus
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

        // Zoom/Pan State (transformable)
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

                                // Größe der Eltern-Box (Bildschirmbereich)
                                val parentW = size.width.toFloat()
                                val parentH = size.height.toFloat()

                                // Ursprüngliche (unskalierte) Größe des Inhalts
                                val unscaledW = laidOut.width.toFloat()
                                val unscaledH = laidOut.height.toFloat()

                                // Aktuelle (skalierte) Größe des Inhalts nach Zoom
                                val scaledW = unscaledW * scale.value
                                val scaledH = unscaledH * scale.value

                                // Mittelpunkt des Inhalts in der Eltern-Box,
                                // verschoben um pan (Translation)
                                val center = Offset(parentW / 2f, parentH / 2f) + pan.value

                                // Berechne die linke obere Ecke des Inhalts
                                val topLeft = center - Offset(scaledW / 2f, scaledH / 2f)

                                // Tap-Koordinate relativ zur linken oberen Ecke des Inhalts
                                val rel = (tapRoot - topLeft)

                                // Wenn der Tap außerhalb des Inhalts liegt → abbrechen
                                if (rel.x !in 0f..scaledW || rel.y !in 0f..scaledH) return@detectTapGestures

                                // Zurückrechnen auf die unskalierte Größe
                                val unscaled = rel / scale.value

                                // Normierte Koordinaten (0..1) innerhalb des Inhalts
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
                            .fillMaxWidth()
                            .aspectRatio(aspect)
                            .onGloballyPositioned { laidOut = it.size }
                    ) {

                        val req = ImageRequest.Builder(context)
                            .data(resolvedImageUri)
                            .size(Size.ORIGINAL)
                            .build()


                        SubcomposeAsyncImage(
                            model = req,
                            contentDescription = "Spraywall",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize(),
                            loading = {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        color = colorResource(R.color.button_normal),
                                        strokeWidth = 3.dp,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            },
                            error = {
                                ImageLoadErrorState()
                            }
                        )

                        val painter = rememberAsyncImagePainter(model = req)
                        val state = painter.state
                        if (state is AsyncImagePainter.State.Success) {
                            val d = state.result.drawable
                            LaunchedEffect(d) {
                                imgW = d.intrinsicWidth.coerceAtLeast(1)
                                imgH = d.intrinsicHeight.coerceAtLeast(1)
                            }
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

                                    // Drag-Loop: solange Finger unten, Position updaten;
                                    // beim Loslassen ggf. über Trash → entfernen

                                    .pointerInput(hold.id, laidOut, scale.value) {
                                        awaitEachGesture {
                                            val down = awaitFirstDown(requireUnconsumed = false)

                                            // sofort: Parent-Transformable blocken
                                            isPointerDownOnHold = true
                                            showTrash = true
                                            overTrash = false

                                            val startHold = uiState.holds.first { it.id == hold.id }
                                            var currentNorm = Offset(startHold.x, startHold.y)
                                            if (uiState.selectedHoldId != hold.id) viewModel.selectHold(hold.id)

                                            down.consume() // Down verbrauchen

                                            var lastFingerWindow: Offset? = holdCoords?.localToWindow(down.position)

                                            try {
                                                while (true) {
                                                    // <<< WICHTIG: Initial-Pass
                                                    val event = awaitPointerEvent(PointerEventPass.Initial)
                                                    val change = event.changes.firstOrNull { it.id == down.id } ?: event.changes.first()

                                                    val deltaPx = change.positionChange()
                                                    change.consume() // selbst konsumieren

                                                    if (laidOut.width != 0 && laidOut.height != 0) {
                                                        val dx = deltaPx.x / (laidOut.width * scale.value)
                                                        val dy = deltaPx.y / (laidOut.height * scale.value)
                                                        currentNorm = Offset(
                                                            (currentNorm.x + dx).coerceIn(0f, 1f),
                                                            (currentNorm.y + dy).coerceIn(0f, 1f)
                                                        )
                                                        viewModel.updateHoldPosition(hold.id, currentNorm.x, currentNorm.y)
                                                    }

                                                    val fingerInWindow = holdCoords?.localToWindow(change.position)
                                                    if (fingerInWindow != null) lastFingerWindow = fingerInWindow
                                                    overTrash = trashBounds?.contains(fingerInWindow ?: lastFingerWindow ?: Offset.Zero) == true

                                                    if (!change.pressed) break
                                                }
                                            } finally {
                                                if (trashBounds?.contains(lastFingerWindow ?: Offset.Zero) == true) {
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
                shape = RoundedCornerShape(24.dp),
                containerColor = Color.White.copy(alpha = 0.7f),
                tonalElevation = 6.dp,


                title = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (mode is BoulderScreenMode.Edit) "Boulder bearbeiten" else "Boulder erstellen",
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(6.dp))
                        // kleiner „Accent“-Strich in button_normal für visuelles Highlight
                        Box(
                            modifier = Modifier
                                .width(70.dp)
                                .height(4.dp)
                                .background(colorResource(R.color.button_normal), RoundedCornerShape(2.dp))
                        )
                    }
                },

                text = {
                    // Feld-Style wie in deinen Screens
                    val tfColors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White.copy(alpha = 0.8f),
                        unfocusedContainerColor = Color.White.copy(alpha = 0.7f),
                        focusedBorderColor = colorResource(R.color.button_normal),
                        cursorColor = colorResource(R.color.button_normal),
                        focusedLabelColor = colorResource(R.color.button_normal)
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                        // Name
                        OutlinedTextField(
                            value = boulderName,
                            onValueChange = { new ->
                                if (new.length <= MAX_NAME) boulderName = new
                            },
                            label = { Text("Name") },
                            isError = triedSave && !isNameValid,
                            supportingText = {
                                if (triedSave && !isNameValid) Text("Bitte einen Namen eingeben")
                            },
                            singleLine = true,
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null,
                                tint = colorResource(R.color.button_normal)) },
                            shape = RoundedCornerShape(50),
                            colors = OutlinedTextFieldDefaults.colors(
                                // internen Container ausschalten
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                // (rest wie gehabt)
                                focusedBorderColor = Color(0xFF00796B),
                                cursorColor = Color(0xFF00796B),
                                focusedLabelColor = Color(0xFF00796B)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Schwierigkeits-Stepper
                        val fbGrades = listOf(
                            "3","4","5A","5B","5C",
                            "6A","6A+","6B","6B+","6C","6C+",
                            "7A","7A+","7B","7B+","7C","7C+",
                            "8A","8A+","8B","8B+","8C","8C+","9A"
                        )

                        DifficultyStepper(
                            options = fbGrades,
                            value = boulderDifficulty.ifEmpty { fbGrades.first() },
                            onValueChange = { boulderDifficulty = it }
                        )

                        // Setter-Notiz
                        OutlinedTextField(
                            value = setterNote,
                            onValueChange = { new -> if (new.length <= MAX_NOTE) setterNote = new },
                            label = { Text("Setter-Notiz (optional)") },
                            supportingText = { Text("${setterNote.length} / $MAX_NOTE") },
                            minLines = 2,
                            maxLines = 5,
                            leadingIcon = { Icon(Icons.Default.Info, contentDescription = null,
                                tint = colorResource(R.color.button_normal)) },
                            shape = RoundedCornerShape(20),
                            colors = OutlinedTextFieldDefaults.colors(
                                // internen Container ausschalten
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                // (rest wie gehabt)
                                focusedBorderColor = Color(0xFF00796B),
                                cursorColor = Color(0xFF00796B),
                                focusedLabelColor = Color(0xFF00796B)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },

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

                        if (!com.example.sprayconnectapp.ui.screens.isOnline(context)) {
                            Toast.makeText(
                                context,
                                "Boulder können nur online gespeichert/bearbeitet werden.",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@TextButton
                        }

                        if (mode is BoulderScreenMode.Edit) {
                            viewModel.updateBoulder(
                                context = context,
                                name = boulderName,
                                difficulty = boulderDifficulty,
                                spraywallId = spraywallId,
                                boulderIdOverride = mode.boulderId,
                                setterNote = setterNote

                            )
                            Toast
                                .makeText(context, "Boulder aktualisiert", Toast.LENGTH_SHORT)
                                .show()
                        } else {
                            viewModel.saveBoulder(
                                context = context,
                                name = boulderName,
                                difficulty = boulderDifficulty,
                                spraywallId = spraywallId,
                                setterNote = setterNote
                            )
                            Toast
                                .makeText(context, "Boulder erstellt", Toast.LENGTH_SHORT)
                                .show()
                        }

                        showDialog = false
                        onSave()
                        onBack() // Einen Screen zurück
                        if (fromPicker) onBack() // Nur wenn wir vom Picker kommen noch einen Schritt zurück

                    },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = colorResource(R.color.button_normal)
                        )

                    ) {
                        Text(if (mode is BoulderScreenMode.Edit) "Speichern" else "Anlegen")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) {
                        Text("Abbrechen", color = Color(0xFFD32F2F))
                    }                },
            )
        }

        // Löschen-Dialog (nur im Edit)
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

        if (showOnboarding) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xAA000000)) // halbtransparentes Overlay
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        "Willkommen bei SprayConnect ",
                        color = Color.White,
                        style = androidx.compose.material3.MaterialTheme.typography.headlineMedium
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "📍 Tipp: Drücke lange auf das Bild, um einen Hold hinzuzufügen.\n\n" +
                                "✋ Ziehe einen Hold, um ihn zu verschieben.\n\n" +
                                "🗑️ Ziehe ihn in die obere Ecke, um ihn zu löschen.\n\n" +
                                "💾 Speichere deinen Boulder über das Häkchen oben rechts.",
                        color = Color.White,
                        style = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
                        lineHeight = 22.sp
                    )
                    Spacer(Modifier.height(24.dp))
                    TextButton(
                        onClick = {
                            showOnboarding = false

                            context.getSharedPreferences("prefs", Context.MODE_PRIVATE)
                                .edit().putBoolean("showOnboarding", false).apply()
                        },
                        modifier = Modifier
                            .background(Color.White, shape = CircleShape)
                            .padding(horizontal = 24.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "Loslegen ",
                            color = Color.Black,
                            style = androidx.compose.material3.MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
        }


    }


}

/**
 * Liest Breite/Höhe eines lokalen Bildes und berücksichtigt die EXIF-Orientierung.
 * Bei 90°/270° werden w/h getauscht.
 */

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




@Composable
private fun ImageLoadErrorState(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.ImageSearch,
                contentDescription = null,
                tint = colorResource(R.color.button_normal_dark),
                modifier = Modifier.size(72.dp)
            )
            Text(
                "Bild konnte nicht geladen werden",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White
            )
            Text(
                "Bitte überprüfe deine Internetverbindung oder versuche es später erneut.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.85f),
                textAlign = TextAlign.Center
            )
        }
    }
}


