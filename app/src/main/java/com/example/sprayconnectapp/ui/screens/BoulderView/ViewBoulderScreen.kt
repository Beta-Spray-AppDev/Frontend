package com.example.sprayconnectapp.ui.screens.BoulderView

import android.graphics.BitmapFactory
import androidx.exifinterface.media.ExifInterface
import android.net.Uri
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BarChart

import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NavigateBefore
import androidx.compose.material.icons.filled.NavigateNext
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.outlined.Star

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Size
import com.example.sprayconnectapp.R
import com.example.sprayconnectapp.data.dto.HoldType
import com.example.sprayconnectapp.ui.screens.BoulderList.BoulderListViewModel
import com.example.sprayconnectapp.ui.screens.Profile.ProfileViewModel


import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.saveable.rememberSaveable

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.example.sprayconnectapp.data.dto.BoulderDTO
import com.example.sprayconnectapp.util.TokenStore
import java.text.DateFormat
import java.util.Date
import kotlin.math.roundToInt




/**
 * Viewer für einen Boulder:
 * - Zeigt Hintergrundbild + Holds
 * - Ermöglicht Zoomen/Schwenken
 * - Navigation zu vorherigem/nächsten Boulder aus der Liste, aus der man kam
 * - FAB "Bearbeiten" nur für den Setter
 */


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ViewBoulderScreen(
    navController: NavController,
    boulderId: String,
    spraywallId: String,
    imageUri: String,
    source: String, // Quelle des Aufrufs
    onBack: () -> Unit, // Callback für zurück - navigation
    viewModel: CreateBoulderViewModel = viewModel()
) {
    // voriger BackStack-Eintrag (BoulderList oder Profile)
    val prevEntry = navController.previousBackStackEntry

    // wenn man von BoulderList kommt - liste aus diesem Viewmodel
    val listVm = prevEntry?.let { viewModel<BoulderListViewModel>(it) }
    val gymList = listVm?.boulders?.value ?: emptyList()


    //Falls man vom Profil kommt
    val profileVm: ProfileViewModel? = prevEntry?.let { viewModel(it) }

    // eigene Boulder
    val myList = profileVm?.myBoulders?.collectAsState()?.value ?: emptyList()

    // Getickte Boulder (einfach als BoulderDTO-Liste vom ViewModel)
    val tickedBoulders = profileVm?.myTicks?.collectAsState()?.value ?: emptyList()
    val tickedIds: Set<String> = remember(tickedBoulders) { tickedBoulders.mapNotNull { it.id }.toSet() }


    var currentBoulderId by rememberSaveable { mutableStateOf(boulderId) }


    // Wählt Liste nach Quelle
    val fromProfile = source == "mine" || source == "ticked"

    val list: List<BoulderDTO> = when {
        !fromProfile -> gymList
        source == "mine" -> myList
        else -> tickedBoulders // <- das ist unsere echte Liste der getickten Boulder
    }

    //Ui-State holen
    val context = LocalContext.current
    val uiState by viewModel.uiState
    val boulder = uiState.boulder
    val density = LocalDensity.current
    val markerSizeDp = 32.dp
    val markerRadiusPx = with(density) { (markerSizeDp / 2).toPx() }

    // Prev & Next
    val currentId = boulder?.id
    val idx = list.indexOfFirst { it.id == currentId }
    val prevId = if (idx > 0) list[idx - 1].id else null
    val nextId = if (idx >= 0 && idx + 1 < list.size) list[idx + 1].id else null

    // Dialogzustände
    var showTickDialog by remember { mutableStateOf(false) }
    var showInfo by remember { mutableStateOf(false) }

    // Bildgröße für Aspect Ratio
    var imgW by remember { mutableStateOf(1) }
    var imgH by remember { mutableStateOf(1) }
    var laidOut by remember { mutableStateOf(IntSize.Zero) }

    // Repository init + Boulder laden (einmalig, konfliktfrei)
    LaunchedEffect(currentBoulderId) {
        viewModel.initRepository(context)
        viewModel.loadBoulder(context, currentBoulderId)
    }

    // Bildmaße (inkl. EXIF) bestimmen
    LaunchedEffect(imageUri) {
        if (imageUri.isBlank()) return@LaunchedEffect
        val (w, h) = readImageSizeRespectingExif(context, imageUri.toUri())
        imgW = w
        imgH = h
    }

    val aspect = remember(imgW, imgH) { imgW.toFloat() / imgH.toFloat() }

    // Farben/Background wie im BoulderListScreen
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
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = BarColor,
                        scrolledContainerColor = BarColor, // gleich bei Scroll
                        titleContentColor = Color.White, // Titelfarbe
                        navigationIconContentColor = Color.White,
                        actionIconContentColor = Color.White
                    ),

                    // Boulder und Grad Titel
                    title = {Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = boulder?.name ?: "/",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis, // Ellipsen bei Überlänge ...
                            modifier = Modifier.fillMaxWidth().basicMarquee(), // Lauftext wenn zu lang
                            textAlign = TextAlign.Center

                        )
                        Text(
                            text = "Grad: ${boulder?.difficulty ?: "Unbekannt"}",
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
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
            // Bearbeiten Button nur für den Ersteller
            floatingActionButton = {
                val store = TokenStore(context)
                val currentUserId = store.getUserId()

                // Achtung: createdBy kann UUID sein -> toString() für sicheren Vergleich
                val isOwner = boulder?.createdBy?.toString() == currentUserId
                val canEdit = isOwner || store.isSuperUser()

                if (canEdit) {
                    FloatingActionButton(
                        containerColor = Color(0xFF7FBABF),
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
            // Untere Leiste: Prev / Tick / Next
            bottomBar = {
                BottomAppBar(
                    containerColor = BarColor,
                    contentColor = Color.White,
                    tonalElevation = 0.dp,
                    contentPadding = PaddingValues(horizontal = 12.dp)
                ) {
                    val iconSize = 35.dp

                    // Prev Button
                    IconButton(
                        enabled = prevId != null,
                        onClick = { prevId?.let { currentBoulderId = it } }
                    ) {
                        Icon(
                            Icons.Default.NavigateBefore,
                            contentDescription = "Vorheriger Boulder",
                            modifier = Modifier.size(iconSize)
                        )
                    }

                    Spacer(Modifier.weight(1f))

                    // Tick Button
                    IconButton(
                        enabled = boulder?.id != null,
                        onClick = { showTickDialog = true }
                    ) {
                        Icon(Icons.Default.Done, contentDescription = "Eintragen", modifier = Modifier.size(iconSize))
                    }

                    Spacer(Modifier.weight(1f))

                    // Next Button
                    IconButton(
                        enabled = nextId != null,
                        onClick = { nextId?.let { currentBoulderId = it } }
                    ) {
                        Icon(
                            Icons.Default.NavigateNext,
                            contentDescription = "Nächster Boulder",
                            modifier = Modifier.size(iconSize)
                        )
                    }
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
                // Transformierter Inhalt (Bild + Holds)
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
                            Text("Kein Bild verfügbar", modifier = Modifier.align(Alignment.Center))
                        }

                        // Holds zeichnen
                        uiState.boulder?.holds?.forEach { hold ->
                            val color = HoldType.valueOf(hold.type).color
                            val posX = hold.x * laidOut.width
                            val posY = hold.y * laidOut.height

                            Box(
                                modifier = Modifier
                                    .offset {
                                        IntOffset(
                                            (posX - markerRadiusPx).roundToInt(),
                                            (posY - markerRadiusPx).roundToInt()
                                        )
                                    }
                                    .size(markerSizeDp)
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

    // Info-Dialog
    if (showInfo) {
        val created = formatDate(uiState.boulder?.createdAt)
        val updated = formatDate(uiState.boulder?.lastUpdated)
        val difficulty = uiState.boulder?.difficulty ?: "-"

        AlertDialog(
            onDismissRequest = { showInfo = false },
            confirmButton = { TextButton(onClick = { showInfo = false }) { Text("OK", color = colorResource(R.color.button_normal)) } },
            title = { Text(uiState.boulder?.name ?: "Boulder") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    InfoLine("Setter", uiState.boulder?.createdByUsername
                        ?: uiState.boulder?.createdBy?.take(8) ?: "-", Icons.Default.Person)
                    InfoLine("Schwierigkeit", difficulty, Icons.Default.BarChart)
                    InfoLine("Gym", boulder?.gymName ?: "-", Icons.Filled.Place)
                    InfoLine("Spraywall", boulder?.spraywallName ?: "-", Icons.Default.GridOn)

                    Spacer(Modifier.height(4.dp))
                    Divider()
                    InfoLine("Erstellt am", created)
                    InfoLine("Zuletzt aktualisiert", updated)
                }
            }
        )
    }

    // Tick-Dialog
    if (showTickDialog) {

        var stars by remember { mutableStateOf(3) } // Default 3/5
        val fbGrades = listOf(
            "3", "4", "5A", "5B", "5C",
            "6A", "6A+", "6B", "6B+", "6C", "6C+",
            "7A", "7A+", "7B", "7B+", "7C", "7C+",
            "8A", "8A+", "8B", "8B+", "8C", "8C+", "9A"
        )

        var proposed by remember { mutableStateOf(boulder?.difficulty ?: fbGrades.first()) }


        val title = "Boulder eintragen?"
        val name = boulder?.name ?: "diesen Boulder"
        AlertDialog(
            onDismissRequest = { showTickDialog = false },
            title = { Text(title) },
            text = { Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                StarRating(value = stars, onChange = { stars = it })

                Divider()

                DifficultyStepper(
                    options = fbGrades,
                    value = proposed.ifEmpty { fbGrades.first() },
                    onValueChange = { proposed = it }
                )

            } },
            confirmButton = {
                TextButton(
                    onClick = {
                        showTickDialog = false
                        boulder?.id?.let { viewModel.tickBoulder(
                            context = context,
                            boulderId = it,
                            stars = stars,
                            proposedGrade = proposed) }
                    }
                ) { Text("Ja, eintragen",color = colorResource(R.color.button_normal)) }
            },
            dismissButton = { TextButton(onClick = { showTickDialog = false }) { Text("Abbrechen", color = Color(0xFFD32F2F)) } }
        )
    }
}


/** Einfache Info-Zeile im Dialog: Icon + Label + Value */
@Composable
private fun InfoLine(label: String, value: String, icon: ImageVector? = null) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp)

    ){
        Row(Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically) {

            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color(0xFF3F888F),
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(Modifier.width(8.dp))

            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF3F888F),
                modifier = Modifier.weight(1f)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
            )
        }
    }


}




@Composable
fun StarRating(
    value: Int,
    onChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    max: Int = 5
) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        repeat(max) { i ->
            val filled = i < value
            IconButton(onClick = { onChange(i + 1) }) {
                Icon(
                    imageVector = if (i + 1 <= value) Icons.Filled.Star else Icons.Outlined.Star,                    contentDescription = null,
                    tint = if (i + 1 <= value) Color(0xFFFFC107) else Color(0xFFBDBDBD)
                )
            }
        }
    }
}







// Datumshilfe
private fun formatDate(ms: Long?): String {
    if (ms == null) return "-"
    val df = DateFormat.getDateInstance()
    return df.format(Date(ms))
}

/**
 * Liest Bildbreite/-höhe inkl. EXIF-Orientierung.
 * Bei 90°/270° werden Width/Height getauscht.
 */


private fun readImageSizeRespectingExif(
    context: android.content.Context,
    uri: android.net.Uri
): Pair<Int, Int> {
    val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    context.contentResolver.openInputStream(uri)?.use { input ->
        BitmapFactory.decodeStream(input, null, opts)
    }
    var w = if (opts.outWidth > 0) opts.outWidth else 1
    var h = if (opts.outHeight > 0) opts.outHeight else 1

    val orientation = context.contentResolver.openInputStream(uri)?.use { input ->
        ExifInterface(input).getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL
        )
    } ?: ExifInterface.ORIENTATION_NORMAL

    val needsSwap = orientation == ExifInterface.ORIENTATION_ROTATE_90 ||
            orientation == ExifInterface.ORIENTATION_TRANSPOSE ||
            orientation == ExifInterface.ORIENTATION_TRANSVERSE ||
            orientation == ExifInterface.ORIENTATION_ROTATE_270
    if (needsSwap) { val t = w; w = h; h = t }

    return w to h
}
