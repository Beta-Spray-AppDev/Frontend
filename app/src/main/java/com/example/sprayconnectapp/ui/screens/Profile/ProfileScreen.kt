package com.example.sprayconnectapp.ui.screens.Profile

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.sprayconnectapp.data.dto.BoulderDTO
import com.example.sprayconnectapp.data.dto.UserProfile
import com.example.sprayconnectapp.ui.screens.BottomNavigationBar

import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.Dp
import com.example.sprayconnectapp.R
import com.example.sprayconnectapp.util.getPrivateImageFileByName
import com.example.sprayconnectapp.util.localOutputNameFromPreview

import androidx.compose.foundation.lazy.items

import com.example.sprayconnectapp.BuildConfig
import com.example.sprayconnectapp.util.UpdateChecker
import kotlinx.coroutines.launch

import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.example.sprayconnectapp.util.UpdateInstaller
import kotlinx.coroutines.launch
import kotlin.text.contains



/**
 * Profilübersicht:
 * - lädt Profil, eigene Boulder, eigene Ticks
 * - Logout, Profil bearbeiten
 * - Navigiert zu Boulder-Details (mit optionaler lokaler Bild-URI)
 */


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController) {
    val context = LocalContext.current
    val viewModel: ProfileViewModel = viewModel()

    val profile by viewModel.profile.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val boulders by viewModel.myBoulders.collectAsState()
    val ticked by viewModel.myTicks.collectAsState()

    val tickedIds = remember(ticked) { ticked.mapNotNull { it.id }.toSet() }



    // Beim ersten Compose Daten laden
    LaunchedEffect(Unit) {
        viewModel.loadProfile(context)
        viewModel.loadMyBoulders(context)
        viewModel.loadMyTicks(context)
    }

    val BarColor = colorResource(id = R.color.hold_type_bar)


    // Farbverlauf Hintergrund
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
    ){




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
                    //Titel
                    title = { Text("Mein Profil") },

                    //Back Button
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }, ) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Zurück")
                        }
                    },

                    // Logout Button
                    actions = {
                        IconButton(onClick = {
                            viewModel.logout(context)
                            navController.navigate("login") {
                                popUpTo("home") { inclusive = true }
                            }
                        }) {
                            Icon(Icons.Default.ExitToApp, contentDescription = "Logout", modifier = Modifier.size(28.dp) )
                        }
                    }

                )

            },
            bottomBar = { BottomNavigationBar(navController) }
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    when {
                        isLoading -> {
                            // Spinner mittig anzeigen
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = colorResource(R.color.button_normal))
                            }
                        }

                        // Fehlermeldung anzeigen
                        error != null -> {
                            Text("Fehler: $error", color = MaterialTheme.colorScheme.error)
                        }

                        //Erfolgszustand: Profil + Listenbereiche anzeigen
                        profile != null -> {

                            ProfileCard(profile = profile!!, navController = navController)
                            Spacer(modifier = Modifier.height(17.dp))

                            BoulderListCard(title = "Meine Boulder", boulders = boulders, navController = navController, source = "mine", tickedIds = tickedIds, onDeleteSelected =
                                { ids ->
                                viewModel.deleteBoulders(context, ids) {
                                    Toast.makeText(context, "Boulder gelöscht", Toast.LENGTH_SHORT).show()
                                }
                            },
                                onAfterDelete = {})

                            Spacer(Modifier.height(17.dp))

                            val tickGrades by viewModel.myTickGrades.collectAsState()


                            BoulderListCard(title = "Getickte Boulder", boulders = ticked, navController = navController, source = "ticked", tickedIds = tickedIds, userGrades = tickGrades,  onDeleteSelected =
                                { ids ->
                                viewModel.deleteTicks(context, ids) {
                                    Toast.makeText(context, "Tick(s) entfernt", Toast.LENGTH_SHORT).show()
                                }
                            },
                                onAfterDelete = {})

                        }




                        // Kein Zustand
                        else -> {
                            Text("Keine Profildaten vorhanden.")
                        }
                    }
                }

                item {
                    ProfileUpdateCard()
                }
            }
        }



    }

}


/** Zweispaltige Infozeile (Label links, Wert rechts). */
@Composable
fun ProfileInfoRow(label: String, value: String) {

    //zweispaltige Zeile
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium
        )
    }
}





// Karte mit den Profildaten + Button
@Composable
fun ProfileCard(profile: UserProfile, navController: NavController) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Nutzerdaten", style = MaterialTheme.typography.headlineSmall)
            Divider()
            ProfileInfoRow(label = "Benutzername", value = profile.username)
            ProfileInfoRow(label = "E-Mail", value = profile.email ?: "Keine E-Mail hinterlegt")

            Spacer(modifier = Modifier.height(16.dp))
            Button(
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(R.color.button_normal),
                    contentColor = Color.White
                ),
                onClick = { navController.navigate("editProfile") },
                modifier = Modifier.fillMaxWidth()
                    .wrapContentWidth()
            ) {
                Text("Profil bearbeiten")
            }
        }
    }
}



// Karte für einzelnen Boulder
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun BoulderCard(
    displayedDifficulty: String? = null,
    boulder: BoulderDTO,
    isTicked: Boolean = false,
    selectionMode: Boolean,
    isSelected: Boolean,
    onToggleSelect: () -> Unit,
    onLongPressStartSelection: () -> Unit,
    onClick: (() -> Unit)? = null
) {

    val tickArea = 28.dp      // Icon-Größe
    val tickSpacing = 2.dp   // Abstand vor dem Icon

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = {
                    if (selectionMode) onToggleSelect() else onClick?.invoke()
                },
                onLongClick = { if (!selectionMode) onLongPressStartSelection() }
            ),

        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                if (selectionMode) {
                    Checkbox(checked = isSelected, onCheckedChange = { onToggleSelect() },
                        colors = CheckboxDefaults.colors(
                        checkedColor = colorResource(R.color.button_normal),   // Füllung wenn angehakt
                        uncheckedColor = Color.Gray,                          // Rahmen wenn nicht angehakt
                        checkmarkColor = Color.White                          // Häkchen-Farbe
                    ))
                    Spacer(Modifier.width(8.dp))
                }

                Column(Modifier.weight(1f) .padding(end = tickSpacing + tickArea)) {
                    Text(boulder.name, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis, softWrap = false)
                    Spacer(Modifier.height(3.dp))
                    Text(
                        "Schwierigkeit: ${displayedDifficulty ?: boulder.difficulty}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                // Fester Iconbereich (immer da, auch wenn leer)
                Box(Modifier.size(tickArea), contentAlignment = Alignment.Center) {
                    if (isTicked) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Getickt",
                            tint = colorResource(R.color.button_normal),
                            modifier = Modifier.size(tickArea)
                        )
                    }
                }
            }
        }
    }
}



/**
 * Karte mit einer Liste von Bouldern (scrollbar, begrenzte Höhe).
 * - Erzeugt bei Klick die Route zur Detailansicht
 * - Wenn lokales Bild gefunden wird, wird dessen URI als Query-Arg mitgegeben
 */


@Composable
fun BoulderListCard( title: String, boulders: List<BoulderDTO>, source: String,  navController: NavController, maxHeight: Dp = 240.dp, tickedIds: Set<String> = emptySet(), onDeleteSelected: suspend (List<String>) -> Unit,
                     onAfterDelete: () -> Unit, isDeleting: Boolean = false, userGrades: Map<String, String> = emptyMap()) {


    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var selectionMode by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf<Set<String>>(emptySet()) }
    var showConfirm by remember { mutableStateOf(false) }



    fun toggleSelection(id: String) {
        selected = if (id in selected) selected - id else selected + id
        // nicht automatisch selectionMode = false setzen
    }


    fun clearSelection() {
        selectionMode = false
        selected = emptySet()
    }


    fun startSelectionWith(id: String) {
        selectionMode = true
        selected = setOf(id)
    }



    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            ListHeader(
                title = title,
                selectionMode = selectionMode,
                selectedCount = selected.size,
                onCloseSelection = { clearSelection() },
                onDeleteClick = {
                    showConfirm = true
                },
                onSelectAll = {
                    val allIds = boulders.mapNotNull { it.id }.toSet()
                    selected = if (selected.size == allIds.size) emptySet() else allIds
                    selectionMode = selected.isNotEmpty()
                }
            )

            Divider()

            if (boulders.isEmpty()) {
                Text("Keine Einträge gefunden.")
            } else {

                // Scrollbare Liste innerhalb der Karte
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = maxHeight),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 4.dp)
                ) {

                    // key für feste identität statt nur position - kann sich sonst verändern bei Änderungen
                    items(
                        boulders,
                        key = {
                            it.id ?: "fallback-${it.spraywallId}-${it.createdAt}"
                        }) { boulder ->

                        val id = boulder.id ?: return@items

                        val isTicked = boulder.id?.let { tickedIds.contains(it) } == true
                        val isSelected = selected.contains(id)

                        val displayDifficulty = if (source == "ticked") {
                            userGrades[id] ?: boulder.difficulty
                        } else {
                            boulder.difficulty
                        }




                        BoulderCard(
                            boulder = boulder, isTicked = isTicked,
                            selectionMode = selectionMode,
                            isSelected = isSelected,
                            onToggleSelect = { toggleSelection(id) },
                            onLongPressStartSelection = { startSelectionWith(id) },
                            displayedDifficulty = displayDifficulty

                        ) {
                            if (!selectionMode) {


                                // OnClick eines Boulder-Items -> zur Detailansicht navigieren

                                // 1) Preview-URL -> lokalen Dateinamen bestimmen
                                val preview = (boulder.spraywallImageUrl ?: "").trim()
                                val token = Regex("/s/([^/]+)/").find(preview)?.groupValues?.get(1)

                                // Basis-Route mit leerem imageUri-Segment am Ende
                                val base = "view_boulder/${boulder.id}/${boulder.spraywallId}"

                                // Wenn wir keine valide Preview haben, brech ab (kein Download hier!)
                                if (preview.isEmpty() || token.isNullOrEmpty()) {

                                    Toast.makeText(
                                        context,
                                        "Kein lokales Bild gefunden",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    navController.navigate("$base?src=$source")
                                    return@BoulderCard
                                }

                                // 2) Lokale Datei wie in SpraywallDetail benennen und nachschauen
                                val outName = localOutputNameFromPreview(preview, token)
                                val file = getPrivateImageFileByName(context, outName)


                                // versucht lokal vorhandene BildUri zu bauen für den ZielScreen
                                val encodedImage =
                                    if (!preview.isNullOrEmpty() && !token.isNullOrEmpty()) {
                                        val outName = localOutputNameFromPreview(
                                            preview,
                                            token
                                        ) // Dateiname ableiten
                                        val file = getPrivateImageFileByName(
                                            context,
                                            outName
                                        ) //Datei Object im App Speicher ermitteln
                                        // falls datei existiert in uri umwandeln
                                        if (file.exists()) Uri.encode(
                                            Uri.fromFile(file).toString()
                                        ) else ""
                                    } else ""


                                // Route für Detailansicht
                                val route = buildString {
                                    append("$base?src=$source")
                                    // wenn lokale URi dann mitgeben
                                    if (encodedImage.isNotEmpty()) append("&imageUri=$encodedImage")
                                }

                                navController.navigate(route)

                            }


                        }
                    }
                }

                if (showConfirm) {
                    val count = selected.size
                    val isTickDelete = source == "ticked"

                    // Nomen + Verb
                    val nounSingular = if (isTickDelete) "Tick" else "Boulder"
                    val nounPlural   = if (isTickDelete) "Ticks" else "Boulder"
                    val verbInf      = if (isTickDelete) "entfernen" else "löschen"

                    // Titel & Body sauber formulieren
                    val titleText = if (count == 1)
                        "$nounSingular ${verbInf.replaceFirstChar { it.uppercase() }}?"
                    else
                        "$nounPlural ${verbInf.replaceFirstChar { it.uppercase() }}?"

                    val bodyText = "Dieser Vorgang kann nicht rückgängig gemacht werden."

                    AlertDialog(
                        onDismissRequest = { if (!isDeleting) showConfirm = false },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier
                                    .size(48.dp)
                                    .padding(bottom = 8.dp)
                            )
                        },
                        title = {
                            Text(
                                text = titleText,
                                style = MaterialTheme.typography.headlineSmall,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 4.dp),
                                textAlign = TextAlign.Center
                            )
                        },
                        text = {
                            Text(
                                text = bodyText,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp),
                                textAlign = TextAlign.Center
                            )
                        },
                        dismissButton = {
                            OutlinedButton(
                                onClick = { showConfirm = false },
                                enabled = !isDeleting,
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = colorResource(R.color.button_normal)
                                ),
                                border = BorderStroke(1.dp, colorResource(R.color.button_normal))
                            ) { Text("Abbrechen") }
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    val ids = selected.toList()
                                    scope.launch {
                                        try {
                                            onDeleteSelected(ids)
                                            clearSelection()
                                            onAfterDelete()
                                        } finally {
                                            showConfirm = false
                                        }
                                    }
                                },
                                enabled = !isDeleting,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error,
                                    contentColor = MaterialTheme.colorScheme.onError
                                )
                            ) { Text(if (isTickDelete) "Entfernen" else "Löschen") }
                        },
                        modifier = Modifier.widthIn(min = 300.dp) // etwas breiter, sieht runder aus
                    )
                }




            }
        }
    }
}


@Composable
private fun ListHeader(
    title: String,
    selectionMode: Boolean,
    selectedCount: Int,
    onCloseSelection: () -> Unit,
    onDeleteClick: () -> Unit,
    onSelectAll: (() -> Unit)? = null
) {
    // weicher Wechsel zwischen beiden Zuständen
    AnimatedContent(targetState = selectionMode, label = "headerAnim") { selecting ->
        if (!selecting) {
            // Normalzustand
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(title, style = MaterialTheme.typography.headlineSmall)
            }
        } else {
            // Auswahlmodus
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // links: X (Auswahl beenden)
                IconButton(onClick = onCloseSelection) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Auswahl beenden"
                    )
                }

                // Mitte: "n ausgewählt"
                Text(
                    "$selectedCount ausgewählt",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(Modifier.weight(1f))

                // optional: Alle auswählen
                if (onSelectAll != null) {
                    IconButton(onClick = onSelectAll) {
                        Icon(
                            imageVector = Icons.Default.Checklist, // oder Icons.Outlined.DoneAll
                            contentDescription = "Alle auswählen"
                        )
                    }
                }

                // rechts: Löschen
                FilledTonalIconButton(onClick = onDeleteClick, enabled = selectedCount > 0) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Löschen",
                        tint = if (selectedCount > 0) Color(0xFFD32F2F) else Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
fun ProfileUpdateCard() {
    val ctx = LocalContext.current
    var latest by remember { mutableStateOf(UpdatePrefs.readLatest(ctx)) } // Triple<Int, String, Pair<url, log>?>?
    var checking by remember { mutableStateOf(false) }
    // ⬇️ NEU: Download-Status merken (überlebt Recompose durch Prefs-Seed)
    var downloadId by remember {
        mutableStateOf<Long?>(
            UpdatePrefs.getDownloadId(ctx).takeIf { it > 0L }
        )
    }

    val scope = rememberCoroutineScope()

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("App-Update", style = MaterialTheme.typography.headlineSmall)
            Divider()

            val appCode = BuildConfig.VERSION_CODE
            val info = latest
            if (info == null) {
                Text("Keine Update-Information gespeichert.")
            } else {
                val (code, name, pair) = info
                val (url, log) = pair
                val newer = code > appCode

                Text("Aktuelle App-Version: ${BuildConfig.VERSION_NAME} (code $appCode)")
                Text("Verfügbare Version: $name (code $code)")
                if (!log.isNullOrBlank()) Text(log)

                // ⬇️ Fortschrittsanzeige, wenn Download läuft
                if (downloadId != null) {
                    DownloadProgress(
                        downloadId = downloadId!!,
                        onFinished = { uri ->
                            UpdateInstaller.startPackageInstaller(ctx, uri)
                            // Download als „verbraucht“ markieren, damit die Progress-UI verschwindet
                            UpdatePrefs.saveDownloadId(ctx, -1)
                            downloadId = null
                        },
                        onFailed = {
                            android.widget.Toast.makeText(ctx, "Download fehlgeschlagen.", android.widget.Toast.LENGTH_LONG).show()
                            UpdatePrefs.saveDownloadId(ctx, -1)
                            downloadId = null
                        }
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (newer) {
                        Button(
                            enabled = downloadId == null, // während des Downloads deaktiviert
                            onClick = {
                                val id = UpdateInstaller.enqueueDownload(ctx, url)
                                if (id != null) {
                                    downloadId = id
                                    android.widget.Toast
                                        .makeText(ctx, "Download gestartet …", android.widget.Toast.LENGTH_SHORT)
                                        .show()
                                }
                            }
                        ) {
                            Text(if (downloadId == null) "Download & installieren" else "Lädt …")
                        }
                    } else {
                        Text("Du bist auf dem neuesten Stand.")
                    }
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (checking) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                    Text("Prüfe…")
                } else {
                    OutlinedButton(onClick = {
                        checking = true
                        scope.launch {
                            val l = UpdateChecker.fetchLatest(BuildConfig.LATEST_JSON_URL)
                            checking = false
                            if (l != null && !l.apkUrl.isNullOrBlank()) {
                                UpdatePrefs.saveLatest(ctx, l.versionCode, l.versionName, l.apkUrl!!, l.changelog)
                                latest = UpdatePrefs.readLatest(ctx)
                                val newer = l.versionCode > BuildConfig.VERSION_CODE
                                android.widget.Toast.makeText(
                                    ctx,
                                    if (newer) "Update gefunden: ${l.versionName}" else "Kein Update verfügbar",
                                    android.widget.Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                android.widget.Toast.makeText(ctx, "Fehler beim Prüfen", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        }
                    }) { Text("Jetzt prüfen") }
                }
            }
        }
    }
}











