package com.example.sprayconnectapp.ui.screens.BoulderView

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NavigateBefore
import androidx.compose.material.icons.filled.NavigateNext
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.mapSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Size
import com.example.sprayconnectapp.R
import com.example.sprayconnectapp.data.dto.BoulderDTO
import com.example.sprayconnectapp.data.dto.Hold
import com.example.sprayconnectapp.data.dto.HoldType
import com.example.sprayconnectapp.ui.screens.BoulderList.BoulderListViewModel
import com.example.sprayconnectapp.ui.screens.Profile.ProfileViewModel
import com.example.sprayconnectapp.util.TokenStore
import com.example.sprayconnectapp.util.getPrivateImageFileByName
import com.example.sprayconnectapp.util.localOutputNameFromPreview
import java.text.DateFormat
import java.util.Date
import kotlin.math.roundToInt

/**
 * Viewer für einen Boulder:
 * - Zeigt Hintergrundbild + Holds
 * - Stabiler Bildwechsel (kein Springen/Flackern)
 * - Zoom/Schwenk (Reset nur beim echten Boulder-Wechsel)
 * - Navigation zu vorherigem/nächstem Boulder aus der Liste/Profil
 * - FAB "Bearbeiten" nur für den Setter / Superuser
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

    //  – myTicks ist List<TickedItem>
    val tickedItems = profileVm?.myTicks?.collectAsState()?.value ?: emptyList()

    // Set der Boulder-IDs zum Markieren
    val tickedIds: Set<String> = remember(tickedItems) {
        tickedItems.mapNotNull { it.boulderId?.takeIf { it.isNotBlank() } }.toSet()
    }

    var currentBoulderId by rememberSaveable { mutableStateOf(boulderId) }

    //Ui-State holen
    val context = LocalContext.current
    val uiState by viewModel.uiState
    val boulder = uiState.boulder

    var showOnboarding by remember {
        mutableStateOf(
            context.getSharedPreferences("prefs", android.content.Context.MODE_PRIVATE)
                .getBoolean("showOnboarding_view", true)
        )
    }

    // Wählt Liste nach Quelle
    val fromProfile = source == "mine" || source == "ticked"

    // IDs aus dem SavedStateHandle der vorherigen BackStack-Entry holen
    val visibleIdsFromList: List<String> =
        if (!fromProfile && source == "list") {
            navController.previousBackStackEntry
                ?.savedStateHandle
                ?.get<ArrayList<String>>("visibleIds")
                ?.toList()
                ?: emptyList()
        } else emptyList()

    // Aktuelle ID (aus dem geladenen Boulder oder Fallback)
    val visibleId = (uiState.boulder?.id ?: currentBoulderId)

    // Die ID-Liste, durch die geswiped werden darf:
    val idList: List<String> = when {
        source == "list" && visibleIdsFromList.isNotEmpty() -> visibleIdsFromList
        fromProfile -> when (source) {
            "mine"   -> myList.mapNotNull { it.id }
            "ticked" -> tickedItems.mapNotNull { it.boulderId?.takeIf { it.isNotBlank() } }
            else     -> emptyList()
        }
        else -> gymList.mapNotNull { it.id }
    }

    // Index des aktuell sichtbaren Boulders
    val currentIndex = idList.indexOf(visibleId)

    // Vorheriger & nächster Boulder
    val prevId = if (currentIndex > 0) idList[currentIndex - 1] else null
    val nextId = if (currentIndex >= 0 && currentIndex + 1 < idList.size) idList[currentIndex + 1] else null

    // Dialogzustände
    var showTickDialog by remember { mutableStateOf(false) }
    var showInfo by remember { mutableStateOf(false) }

    // Repository init + Boulder laden (einmalig, konfliktfrei)
    LaunchedEffect(currentBoulderId) {
        viewModel.initRepository(context)
        viewModel.loadBoulder(context, currentBoulderId)
    }

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
                        scrolledContainerColor = BarColor,
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White,
                        actionIconContentColor = Color.White
                    ),
                    // Boulder und Grad Titel
                    title = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = boulder?.name ?: "/",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.fillMaxWidth().basicMarquee(),
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "Grad: ${boulder?.difficulty ?: "Unbekannt"}",
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack, modifier = Modifier.padding(start = 8.dp)) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Zurück", modifier = Modifier.size(28.dp))
                        }
                    },
                    actions = {
                        IconButton(onClick = { showInfo = true }, modifier = Modifier.padding(end = 8.dp)) {
                            Icon(Icons.Default.Info, contentDescription = "Info", modifier = Modifier.size(28.dp))
                        }
                    }
                )
            },
            // Bearbeiten Button nur für den Ersteller
            floatingActionButton = {
                val store = TokenStore.create(context)
                val currentUserId = store.getUserId()
                val isOwner = boulder?.createdBy?.toString() == currentUserId
                val canEdit = isOwner || store.isSuperUser()

                if (canEdit) {
                    FloatingActionButton(
                        containerColor = colorResource(R.color.button_normal),
                        onClick = {
                            val encodedUri = Uri.encode(
                                // wenn es ein lokales Cachebild gibt, verwenden wir dessen URI;
                                // sonst das beim Einstieg mitgegebene Bild
                                resolveEffectiveImageUri(
                                    context = context,
                                    boulder = uiState.boulder,
                                    fallbackImageUri = imageUri
                                )
                            )
                            val editTargetId = boulder?.id ?: currentBoulderId
                            if (editTargetId.isNotBlank()) {
                                navController.navigate(
                                    "create_boulder/$spraywallId?imageUri=$encodedUri&mode=edit&boulderId=$editTargetId"
                                )
                            }
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

                    IconButton(
                        enabled = boulder?.id != null,
                        onClick = { showTickDialog = true }
                    ) {
                        Icon(Icons.Default.Done, contentDescription = "Eintragen", modifier = Modifier.size(iconSize))
                    }

                    Spacer(Modifier.weight(1f))

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

            // Effektive Bild-URI bestimmen (lokaler Cache bevorzugt)
            val effectiveImageUri by remember(uiState.boulder?.id, imageUri) {
                mutableStateOf(resolveEffectiveImageUri(context, uiState.boulder, imageUri))
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // Stabiler Bild+Holds-Viewer
                BoulderImageWithHolds(
                    effectiveImageUri = effectiveImageUri,
                    holds = uiState.boulder?.holds.orEmpty(),
                    boulderId = uiState.boulder?.id,
                    modifier = Modifier.align(Alignment.Center),
                    markerDiameterDp = 20,
                    placeholderHeightDp = 300,
                    animateHolds = true
                )
            }
        }
    }

    // Info-Dialog
    if (showInfo) {
        val created = formatDate(uiState.boulder?.createdAt)
        val updated = formatDate(uiState.boulder?.lastUpdated)
        val difficulty = uiState.boulder?.difficulty ?: "-"
        val nameForRoute = boulder?.name.orEmpty()
        val encodedName = Uri.encode(nameForRoute)
        val targetId = boulder?.id

        BoulderInfoDialog(
            show = showInfo,
            onDismiss = { showInfo = false },
            onMoreInfo = targetId?.let {
                { navController.navigate("boulderComments/$targetId?boulderName=$encodedName") }
            },
            title = uiState.boulder?.name ?: "Boulder",
            setter = uiState.boulder?.createdByUsername
                ?: uiState.boulder?.createdBy?.take(8) ?: "-",
            difficulty = difficulty,
            gymName = boulder?.gymName ?: "-",
            spraywallName = boulder?.spraywallName ?: "-",
            created = created,
            updated = updated,
            setterNote = boulder?.setterNote
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
        AlertDialog(
            onDismissRequest = { showTickDialog = false },
            title = { Text(title, color = Color.Black) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    StarRating(value = stars, onChange = { stars = it })

                    Divider(color = Color.Black.copy(alpha = 0.12f))

                    DifficultyStepper(
                        options = fbGrades,
                        value = proposed.ifEmpty { fbGrades.first() },
                        onValueChange = { proposed = it }
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showTickDialog = false
                        boulder?.id?.let {
                            viewModel.tickBoulder(
                                context = context,
                                boulderId = it,
                                stars = stars,
                                proposedGrade = proposed
                            )
                        }
                    }
                ) { Text("Ja, eintragen", color = colorResource(R.color.button_normal)) }
            },
            dismissButton = {
                TextButton(onClick = { showTickDialog = false }) { Text("Abbrechen", color = Color(0xFFD32F2F)) }
            },
            containerColor = Color.White
        )
    }

    if (showOnboarding) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xAA000000))
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    "Farblegende der Griffe",
                    color = Color.White,
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center
                )

                Text(
                    "Die Farben zeigen den Griff-Typ an.",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(8.dp))

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    HoldType.entries.forEach { type ->
                        HoldLegendRow(type)
                    }
                }

                Spacer(Modifier.height(18.dp))

                TextButton(
                    onClick = {
                        showOnboarding = false
                        context.getSharedPreferences("prefs", android.content.Context.MODE_PRIVATE)
                            .edit { putBoolean("showOnboarding_view", false) }
                    },
                    modifier = Modifier
                        .background(Color.White, shape = CircleShape)
                        .padding(horizontal = 22.dp, vertical = 8.dp)
                ) {
                    Text(
                        "Verstanden",
                        color = Color.Black,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}



/** Effektive Bild-URI: nimmt lokalen Cache, wenn vorhanden; sonst Fallback */
private fun resolveEffectiveImageUri(
    context: android.content.Context,
    boulder: BoulderDTO?,
    fallbackImageUri: String
): String {
    val fromBoulder = boulder?.spraywallImageUrl.orEmpty()
    if (fromBoulder.isNotBlank()) {
        val token = Regex("/s/([^/]+)/").find(fromBoulder)?.groupValues?.get(1)
        if (token != null) {
            val outName = localOutputNameFromPreview(fromBoulder, token)
            val file = getPrivateImageFileByName(context, outName)
            if (file.exists()) return Uri.fromFile(file).toString()
        }
        return fromBoulder
    }
    return fallbackImageUri
}

/** Einfache Info-Zeile im Dialog: Icon + Label + Value */
@Composable
private fun InfoLine(label: String, value: String, icon: ImageVector? = null) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = colorResource(R.color.button_normal).copy(alpha = 0.08f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
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

private fun HoldType.prettyName(): String =
    name.lowercase().replace('_', ' ').replaceFirstChar { it.titlecase() }

@Composable
private fun HoldLegendRow(type: HoldType) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(18.dp)
                .border(width = 2.dp, color = type.color, shape = CircleShape)
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = type.prettyName(),
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = Color.White
        )
    }
}

/** Einheitliche Zeile mit Icon, Label links & Wert rechts */
@Composable
fun InfoLineStyled(
    label: String,
    value: String?,
    icon: ImageVector
) {
    if (value.isNullOrBlank()) return

    val accent = colorResource(R.color.button_normal)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = accent,
            modifier = Modifier.size(20.dp)
        )

        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = accent
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF000000)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoulderInfoDialog(
    show: Boolean,
    onDismiss: () -> Unit,
    onMoreInfo: (() -> Unit)? = null,
    // Daten:
    title: String,
    setter: String,
    difficulty: String,
    gymName: String,
    spraywallName: String,
    created: String,
    updated: String,
    setterNote: String? = null
) {
    if (!show) return

    val buttonColor = colorResource(id = R.color.button_normal)

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = Color.White.copy(alpha = 0.75f),
        tonalElevation = 6.dp,
        title = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center,
                    color = Color.Black
                )
                Spacer(Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .width(60.dp)
                        .height(4.dp)
                        .background(colorResource(R.color.button_normal), RoundedCornerShape(2.dp))
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Divider(
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                    modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp)
                )
                InfoLineStyled("Setter", setter, Icons.Default.Person)
                if (!setterNote.isNullOrBlank()) {
                    InfoLineStyled("Setter Notes", setterNote, Icons.Default.EditNote)
                }
                InfoLineStyled("Schwierigkeit", difficulty, Icons.Default.BarChart)
                InfoLineStyled("Gym", gymName, Icons.Default.Place)
                InfoLineStyled("Spraywall", spraywallName, Icons.Default.GridOn)

                Divider(
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                    modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp)
                )
                InfoLineStyled("Erstellt am:", created, Icons.Default.Add)
                InfoLineStyled("Zuletzt aktualisiert:", updated, Icons.Default.AccessTime)
            }
        },
        confirmButton = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (onMoreInfo != null) {
                    TextButton(
                        onClick = {
                            onDismiss()
                            onMoreInfo()
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = buttonColor),
                        shape = RoundedCornerShape(50)
                    ) { Text("Mehr Infos") }
                }

                TextButton(
                    onClick = onDismiss,
                    colors = ButtonDefaults.textButtonColors(contentColor = buttonColor),
                    shape = RoundedCornerShape(50)
                ) { Text("OK") }
            }
        }
    )
}


//Bild+Holds-Viewer


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BoulderImageWithHolds(
    effectiveImageUri: String,
    holds: List<Hold>,
    boulderId: String?,
    modifier: Modifier = Modifier,
    markerDiameterDp: Int = 20,
    placeholderHeightDp: Int = 300,
    animateHolds: Boolean = true,
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val markerSizeDp = markerDiameterDp.dp
    val markerRadiusPx = with(density) { (markerSizeDp / 2).toPx() }

    // Layout-Messung
    var laidOut by remember { mutableStateOf(IntSize.Zero) }
    // Bild-Aspect-Ratio; erst nach onSuccess setzen → kein Layout-Jump
    var imageAspect by remember { mutableStateOf<Float?>(null) }

    //zoom bleibt gleich
    // scale: simple primitive → rememberSaveable reicht
    var scale by rememberSaveable { mutableStateOf(1f) }

    // pan: braucht Saver (Offset ist kein primitives Save-Format)
    val offsetSaver = remember {
        mapSaver<Offset>(
            save = { mapOf("x" to it.x, "y" to it.y) },
            restore = { saved -> Offset((saved["x"] as Float?) ?: 0f, (saved["y"] as Float?) ?: 0f) }
        )
    }
    var pan by rememberSaveable(stateSaver = offsetSaver) { mutableStateOf(Offset.Zero) }


    val tfState = rememberTransformableState { zoom, offset, _ ->
        scale = (scale * zoom).coerceIn(1f, 4f)
        pan += offset
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .transformable(tfState),
        contentAlignment = Alignment.Center
    ) {
        // Transformierter Stack (Bild + Holds)
        Box(
            modifier = Modifier.graphicsLayer {
                transformOrigin = TransformOrigin.Center
                scaleX = scale
                scaleY = scale
                translationX = pan.x
                translationY = pan.y
            }
        ) {
            // Container: stabil mit finaler aspectRatio oder Placeholder-Größe
            Box(
                modifier = Modifier
                    .then(
                        if (imageAspect != null)
                            Modifier
                                .fillMaxWidth()
                                .aspectRatio(imageAspect!!)
                        else
                            Modifier
                                .fillMaxWidth()
                                .height(placeholderHeightDp.dp)
                    )
                    .onGloballyPositioned { laidOut = it.size }
            ) {
                // Bild (Crossfade für weichen Wechsel)
                if (effectiveImageUri.isNotBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(effectiveImageUri)
                            .size(Size.ORIGINAL)
                            .crossfade(true)
                            .listener(
                                onSuccess = { _, result ->
                                    val d = result.drawable
                                    if (d.intrinsicWidth > 0 && d.intrinsicHeight > 0) {
                                        imageAspect = d.intrinsicWidth.toFloat() / d.intrinsicHeight
                                    }
                                }
                            )
                            .build(),
                        contentDescription = "Boulder",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }

                // Holds erst zeichnen, wenn Layout und aspectRatio stabil sind
                val canDrawHolds = laidOut.width > 0 && laidOut.height > 0 && imageAspect != null
                if (canDrawHolds) {
                    holds.forEach { hold ->
                        val color = HoldType.valueOf(hold.type).color
                        val posX = hold.x * laidOut.width
                        val posY = hold.y * laidOut.height

                        val xPx = (posX - markerRadiusPx).roundToInt()
                        val yPx = (posY - markerRadiusPx).roundToInt()

                        val xAnimated = if (animateHolds) animateIntAsState(xPx).value else xPx
                        val yAnimated = if (animateHolds) animateIntAsState(yPx).value else yPx

                        Box(
                            modifier = Modifier
                                .offset { IntOffset(xAnimated, yAnimated) }
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


