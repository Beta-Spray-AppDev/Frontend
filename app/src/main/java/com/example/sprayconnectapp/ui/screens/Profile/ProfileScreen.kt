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
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.sprayconnectapp.BuildConfig
import com.example.sprayconnectapp.R
import com.example.sprayconnectapp.data.dto.BoulderDTO
import com.example.sprayconnectapp.data.dto.TickedItem
import com.example.sprayconnectapp.data.dto.UserProfile
import com.example.sprayconnectapp.ui.screens.BottomNavigationBar
import com.example.sprayconnectapp.util.UpdateChecker
import com.example.sprayconnectapp.util.UpdateInstaller
import com.example.sprayconnectapp.util.getPrivateImageFileByName
import com.example.sprayconnectapp.util.localOutputNameFromPreview
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
    val tickedBoulderIds = remember(ticked) { ticked.mapNotNull { it.boulderId }.toSet() }




    // Beim ersten Compose Daten laden
    LaunchedEffect(Unit) {
        viewModel.loadProfile(context)
        viewModel.loadMyBoulders(context)
        viewModel.loadMyTicks(context)
    }

    val BarColor = colorResource(id = R.color.hold_type_bar)

    // Farbverlauf Hintergrund (hart)
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
                    title = { Text("Mein Profil") },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Zurück")
                        }
                    },
                    actions = {
                        // 🔹 Share-Button
                        IconButton(onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    "Schau dir SprayConnect an! 💪\nHier kannst du die App herunterladen:\nhttps://sprayconnect.at"
                                )
                            }
                            val chooser = Intent.createChooser(shareIntent, "App teilen")
                            try {
                                context.startActivity(chooser)
                            } catch (e: Exception) {
                                Toast.makeText(context, "Konnte Teilen nicht öffnen", Toast.LENGTH_SHORT).show()
                            }
                        }) {
                            Icon(
                                Icons.Default.Share,
                                contentDescription = "App teilen",
                                tint = Color.White
                            )
                        }

                        // 🔹 Logout-Button
                        IconButton(onClick = {
                            viewModel.logout(context)
                            navController.navigate("login") {
                                popUpTo("home") { inclusive = true }
                            }
                        }) {
                            Icon(
                                Icons.Default.ExitToApp,
                                contentDescription = "Logout",
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
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
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = colorResource(R.color.button_normal))
                            }
                        }
                        error != null -> {
                            // Fehlerfarbe kann gerne systemisch bleiben; sonst eigen hartes Rot setzen.
                            Text("Fehler: $error", color = MaterialTheme.colorScheme.error)
                        }
                        profile != null -> {
                            ProfileCard(profile = profile!!, navController = navController)
                            Spacer(modifier = Modifier.height(17.dp))

                            BoulderListCard(
                                title = "Meine Boulder",
                                boulders = boulders,
                                navController = navController,
                                source = "mine",
                                tickedIds = tickedBoulderIds,
                                onDeleteSelected = { ids ->
                                    viewModel.deleteBoulders(context, ids) {
                                        Toast.makeText(context, "Boulder gelöscht", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                onAfterDelete = {}
                            )

                            Spacer(Modifier.height(17.dp))


                            TickedListCard(
                                items = ticked,                      // List<TickedItem>
                                navController = navController,
                                onDeleteSelected = { tickIds ->      // erwartet Tick-IDs
                                    viewModel.deleteTicksByTickIds(context, tickIds) {
                                        Toast.makeText(context, "Tick(s) entfernt", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            )


                        }
                        else -> {
                            Text("Keine Profildaten vorhanden.", color = Color(0xFF000000))
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
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.titleMedium,
            color = Color(0xFF000000)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = Color(0xFF000000)
        )
    }
}

// Karte mit den Profildaten + Button (weiß/schwarz, Akzent = button_normal)
@Composable
fun ProfileCard(profile: UserProfile, navController: NavController) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(6.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFE5E5E5),
            contentColor = Color(0xFF000000)
        )
    ) {
        Column(
            Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Nutzerdaten", style = MaterialTheme.typography.headlineSmall, color = Color(0xFF000000))
            Divider(color = Color(0x1F000000)) // 12% Schwarz
            ProfileInfoRow(label = "Benutzername", value = profile.username)
            ProfileInfoRow(label = "E-Mail", value = profile.email ?: "Keine E-Mail hinterlegt")

            Spacer(modifier = Modifier.height(16.dp))
            Button(
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(R.color.button_normal),
                    contentColor = Color(0xFFF5F5F5)
                ),
                onClick = { navController.navigate("editProfile") },
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentWidth()
            ) {
                Text("Profil bearbeiten")
            }
        }
    }
}

// Karte für einzelnen Boulder (weiß/schwarz, Akzent = button_normal)
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
    val tickArea = 28.dp
    val tickSpacing = 2.dp

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { if (selectionMode) onToggleSelect() else onClick?.invoke() },
                onLongClick = { if (!selectionMode) onLongPressStartSelection() }
            ),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFFFFF),
            contentColor = Color(0xFF000000)
        ),
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
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = { onToggleSelect() },
                        colors = CheckboxDefaults.colors(
                            checkedColor = colorResource(R.color.button_normal),
                            uncheckedColor = Color(0x66000000), // 40% Schwarz
                            checkmarkColor = Color(0xFFFFFFFF)
                        )
                    )
                    Spacer(Modifier.width(8.dp))
                }

                Column(Modifier.weight(1f).padding(end = tickSpacing + tickArea)) {
                    Text(
                        boulder.name,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        softWrap = false,
                        color = Color(0xFF000000)
                    )
                    Spacer(Modifier.height(3.dp))
                    Text(
                        "Schwierigkeit: ${displayedDifficulty ?: boulder.difficulty}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF000000)
                    )
                }

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
fun BoulderListCard(
    title: String,
    boulders: List<BoulderDTO>,
    source: String,
    navController: NavController,
    maxHeight: Dp = 240.dp,
    tickedIds: Set<String> = emptySet(),
    onDeleteSelected: suspend (List<String>) -> Unit,
    onAfterDelete: () -> Unit,
    isDeleting: Boolean = false,
    userGrades: Map<String, String> = emptyMap()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var selectionMode by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf<Set<String>>(emptySet()) }
    var showConfirm by remember { mutableStateOf(false) }

    fun toggleSelection(id: String) {
        selected = if (id in selected) selected - id else selected + id
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
        elevation = CardDefaults.cardElevation(6.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFE5E5E5),
            contentColor = Color(0xFF000000)
        )
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ListHeader(
                title = title,
                selectionMode = selectionMode,
                selectedCount = selected.size,
                onCloseSelection = { clearSelection() },
                onDeleteClick = { showConfirm = true },
                onSelectAll = {
                    val allIds = boulders.mapNotNull { it.id }.toSet()
                    selected = if (selected.size == allIds.size) emptySet() else allIds
                    selectionMode = selected.isNotEmpty()
                }
            )

            Divider(color = Color(0x1F000000)) // 12% Schwarz

            if (boulders.isEmpty()) {
                Text("Keine Einträge gefunden.", color = Color(0xFF000000))
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = maxHeight),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 4.dp)
                ) {
                    items(
                        boulders,
                        key = { it.id ?: "fallback-${it.spraywallId}-${it.createdAt}" }
                    ) { boulder ->
                        val id = boulder.id ?: return@items
                        val isTicked = boulder.id?.let { tickedIds.contains(it) } == true
                        val isSelected = selected.contains(id)

                        val displayDifficulty = if (source == "ticked") {
                            userGrades[id] ?: boulder.difficulty
                        } else {
                            boulder.difficulty
                        }

                        BoulderCard(
                            boulder = boulder,
                            isTicked = isTicked,
                            selectionMode = selectionMode,
                            isSelected = isSelected,
                            onToggleSelect = { toggleSelection(id) },
                            onLongPressStartSelection = { startSelectionWith(id) },
                            displayedDifficulty = displayDifficulty
                        ) {
                            if (!selectionMode) {
                                val preview = (boulder.spraywallImageUrl ?: "").trim()
                                val token = Regex("/s/([^/]+)/").find(preview)?.groupValues?.get(1)
                                val base = "view_boulder/${boulder.id}/${boulder.spraywallId}"

                                if (preview.isEmpty() || token.isNullOrEmpty()) {
                                    Toast.makeText(
                                        context,
                                        "Kein lokales Bild gefunden",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    navController.navigate("$base?src=$source")
                                    return@BoulderCard
                                }

                                val outName = localOutputNameFromPreview(preview, token)
                                val file = getPrivateImageFileByName(context, outName)

                                val encodedImage =
                                    if (preview.isNotEmpty() && token.isNotEmpty()) {
                                        val f = getPrivateImageFileByName(context, outName)
                                        if (f.exists()) Uri.encode(Uri.fromFile(f).toString()) else ""
                                    } else ""

                                val route = buildString {
                                    append("$base?src=$source")
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

                    val nounSingular = if (isTickDelete) "Tick" else "Boulder"
                    val nounPlural = if (isTickDelete) "Ticks" else "Boulder"
                    val verbInf = if (isTickDelete) "entfernen" else "löschen"

                    val titleText = if (count == 1)
                        "$nounSingular ${verbInf.replaceFirstChar { it.uppercase() }}?"
                    else
                        "$nounPlural ${verbInf.replaceFirstChar { it.uppercase() }}?"

                    val bodyText = "Dieser Vorgang kann nicht rückgängig gemacht werden."

                    AlertDialog(
                        onDismissRequest = { if (!isDeleting) showConfirm = false },
                        containerColor = Color(0xFFE5E5E5),
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
                                textAlign = TextAlign.Center,
                                color = Color(0xFF000000)
                            )
                        },
                        text = {
                            Text(
                                text = bodyText,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp),
                                textAlign = TextAlign.Center,
                                color = Color(0xFF000000)
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
                        modifier = Modifier.widthIn(min = 300.dp)
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
    AnimatedContent(targetState = selectionMode, label = "headerAnim") { selecting ->
        if (!selecting) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(title, style = MaterialTheme.typography.headlineSmall, color = Color(0xFF000000))
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onCloseSelection) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Auswahl beenden",
                        tint = Color(0xFF000000)
                    )
                }

                Text(
                    "$selectedCount ausgewählt",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFF000000)
                )

                Spacer(Modifier.weight(1f))

                if (onSelectAll != null) {
                    IconButton(onClick = onSelectAll) {
                        Icon(
                            imageVector = Icons.Default.Checklist,
                            contentDescription = "Alle auswählen",
                            tint = Color(0xFF000000)
                        )
                    }
                }

                FilledTonalIconButton(
                    onClick = onDeleteClick,
                    enabled = selectedCount > 0,
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = Color(0xFFE5E5E5),                 // neutral wie deine Cards
                        contentColor = if (selectedCount > 0)
                            Color(0xFFD32F2F)                               // dein Rot
                        else
                            Color(0x66000000),                              // 40% Schwarz
                        disabledContainerColor = Color(0xFFE5E5E5),
                        disabledContentColor = Color(0x66000000)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Löschen"
                        // kein tint nötig, kommt jetzt vom Button-ContentColor
                    )
                }

            }
        }
    }
}

@Composable
fun ProfileUpdateCard() {
    val ctx = LocalContext.current
    var latest by remember { mutableStateOf(UpdatePrefs.readLatest(ctx)) }
    var checking by remember { mutableStateOf(false) }
    var downloadId by remember {
        mutableStateOf<Long?>(
            UpdatePrefs.getDownloadId(ctx).takeIf { it > 0L }
        )
    }

    val scope = rememberCoroutineScope()

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(6.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFE5E5E5),
            contentColor = Color(0xFF000000)
        )
    ) {
        Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("App-Update", style = MaterialTheme.typography.headlineSmall, color = Color(0xFF000000))
            Divider(color = Color(0x1F000000))

            val appCode = BuildConfig.VERSION_CODE
            val info = latest
            if (info == null) {
                Text("Keine Update-Information gespeichert.", color = Color(0xFF000000))
            } else {
                val (code, name, pair) = info
                val (url, log) = pair
                val newer = code > appCode

                Text("Aktuelle App-Version: ${BuildConfig.VERSION_NAME} (code $appCode)", color = Color(0xFF000000))
                Text("Verfügbare Version: $name (code $code)", color = Color(0xFF000000))
                if (!log.isNullOrBlank()) Text(log, color = Color(0xFF000000))

                if (downloadId != null) {
                    DownloadProgress(
                        downloadId = downloadId!!,
                        onFinished = { uri ->
                            UpdateInstaller.startPackageInstaller(ctx, uri)
                            UpdatePrefs.saveDownloadId(ctx, -1)
                            downloadId = null
                        },
                        onFailed = {
                            android.widget.Toast.makeText(
                                ctx,
                                "Download fehlgeschlagen.",
                                android.widget.Toast.LENGTH_LONG
                            ).show()
                            UpdatePrefs.saveDownloadId(ctx, -1)
                            downloadId = null
                        }
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (newer) {
                        Button(
                            enabled = downloadId == null,
                            onClick = {
                                val id = UpdateInstaller.enqueueDownload(ctx, url)
                                if (id != null) {
                                    downloadId = id
                                    android.widget.Toast
                                        .makeText(ctx, "Download gestartet …", android.widget.Toast.LENGTH_SHORT)
                                        .show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colorResource(R.color.button_normal),
                                contentColor = Color(0xFFFFFFFF)
                            )
                        ) {
                            Text(if (downloadId == null) "Download & installieren" else "Lädt …")
                        }
                    } else {
                        Text("Du bist auf dem neuesten Stand.", color = Color(0xFF000000))
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (checking) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = colorResource(R.color.button_normal))
                    Spacer(Modifier.width(8.dp))
                    Text("Prüfe…", color = Color(0xFF000000))
                } else {
                    OutlinedButton(
                        onClick = {
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
                        },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = colorResource(id = R.color.button_normal)
                        ),
                        border = BorderStroke(1.dp, colorResource(id = R.color.button_normal))
                    ) { Text("Jetzt prüfen") }
                }
            }
        }
    }
}


@Composable
fun TickedListCard(
    title: String = "Getickte Boulder",
    items: List<TickedItem>,
    navController: NavController,
    maxHeight: Dp = 240.dp,
    onDeleteSelected: suspend (List<String>) -> Unit // nimmt Tick-IDs!
) {
    val context = LocalContext.current
    var selectionMode by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf<Set<String>>(emptySet()) } // tickIds
    var showConfirm by remember { mutableStateOf(false) }

    fun toggle(id: String) { selected = if (id in selected) selected - id else selected + id }
    fun startSel(id: String) { selectionMode = true; selected = setOf(id) }
    fun clearSel() { selectionMode = false; selected = emptySet() }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(6.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFE5E5E5),
            contentColor = Color(0xFF000000)
        )
    ) {
        val scope = rememberCoroutineScope()
        Column(Modifier.padding(24.dp)) {
            ListHeader(
                title = title,
                selectionMode = selectionMode,
                selectedCount = selected.size,
                onCloseSelection = { clearSel() },
                onDeleteClick = { showConfirm = true },
                onSelectAll = {
                    val all = items.map { it.tickId }.toSet()
                    selected = if (selected.size == all.size) emptySet() else all
                    selectionMode = selected.isNotEmpty()
                }
            )

            Divider(color = Color(0x1F000000))

            if (items.isEmpty()) {
                Text("Keine Einträge gefunden.", color = Color(0xFF000000))
                return@Column
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = maxHeight),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(items, key = { it.tickId }) { it ->
                    // Für die Card basteln wir ein leichtes Anzeigeobjekt
                    val display = BoulderDTO(
                        id = it.tickId, // nur für Auswahl/Keys – NICHT Boulder-ID!
                        name = it.name,
                        difficulty = it.displayedDifficulty ?: "-"
                    )

                    var lastToastTime by remember { mutableStateOf(0L) }


                    BoulderCard(
                        displayedDifficulty = it.displayedDifficulty ?: "-",
                        boulder = display,
                        isTicked = true,
                        selectionMode = selectionMode,
                        isSelected = it.tickId in selected,
                        onToggleSelect = { toggle(it.tickId) },
                        onLongPressStartSelection = { startSel(it.tickId) }
                    ) {

                        // Navigation: nur wenn Boulder noch existiert
                        if (it.boulderId == null) {

                            val now = System.currentTimeMillis()
                            if (now - lastToastTime > 2000) { // 1 Sekunde Sperre
                                Toast.makeText(context, "Boulder wurde vom Ersteller gelöscht.", Toast.LENGTH_SHORT).show()
                                lastToastTime = now
                            }
                        } else {

                            // Versuch, lokales Bild aus spraywallImageUrl zu finden (wie in BoulderListCard)
                            val preview = (it.spraywallImageUrl ?: "").trim()
                            val token = Regex("/s/([^/]+)/").find(preview)?.groupValues?.get(1)

                            val encodedImage =
                                if (preview.isNotEmpty() && !token.isNullOrEmpty()) {
                                    val outName = localOutputNameFromPreview(preview, token)
                                    val f = getPrivateImageFileByName(context, outName)
                                    if (f.exists()) Uri.encode(Uri.fromFile(f).toString()) else ""
                                } else ""

                            val spraywallId = it.spraywallId ?: ""  // leeren String nur wenn unbedingt nötig
                            val base = "view_boulder/${it.boulderId}/$spraywallId"

                            val route = buildString {
                                append("$base?src=ticked")
                                if (encodedImage.isNotEmpty()) append("&imageUri=$encodedImage")
                            }


                            navController.navigate(route)
                        }
                    }
                }
            }

            if (showConfirm) {
                val count = selected.size
                val titleText = if (count == 1) "Tick Entfernen?" else "Ticks Entfernen?"
                val bodyText = "Dieser Vorgang kann nicht rückgängig gemacht werden."

                AlertDialog(
                    onDismissRequest = { showConfirm = false },
                    containerColor = Color(0xFFE5E5E5),
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
                            textAlign = TextAlign.Center,
                            color = Color(0xFF000000)
                        )
                    },
                    text = {
                        Text(
                            text = bodyText,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp),
                            textAlign = TextAlign.Center,
                            color = Color(0xFF000000)
                        )
                    },
                    dismissButton = {
                        OutlinedButton(
                            onClick = { showConfirm = false },
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
                                    onDeleteSelected(ids)   // suspend-Funktion
                                    clearSel()
                                    showConfirm = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError
                            )
                        ) { Text("Entfernen") }
                    },
                    modifier = Modifier.widthIn(min = 300.dp)
                )
            }
        }
    }
}











