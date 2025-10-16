package com.example.sprayconnectapp.ui.screens.BoulderList

import android.net.Uri
import android.widget.Toast
import kotlin.math.roundToInt
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.sprayconnectapp.R
import com.example.sprayconnectapp.ui.screens.BottomNavigationBar
import com.example.sprayconnectapp.ui.screens.isOnline
import androidx.compose.material.icons.outlined.ImageSearch
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.material.icons.outlined.Star
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.sprayconnectapp.data.dto.BoulderDTO


enum class SortKey { GRADE, STARS }

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun BoulderListScreen(
    navController: NavController,
    spraywallId: String,
    spraywallName: String,
    imageUri: String?
) {
    val context = LocalContext.current
    val viewModel: BoulderListViewModel = viewModel()


    // minimal gewünschte Sterne (0 = egal)

    var contextMenuForId by remember { mutableStateOf<String?>(null) }

    val tokenStore = remember { com.example.sprayconnectapp.util.TokenStore.create(context) }
    val myUserId = remember { tokenStore.getUserId() }
    val isSuper = remember { tokenStore.isSuperUser() }
    fun canDelete(b: BoulderDTO): Boolean = isSuper || (myUserId != null && myUserId == b.createdBy)
    var showDeleteDialog by remember { mutableStateOf(false) }
    var boulderToDelete by remember { mutableStateOf<String?>(null) }


    // UI-State aus dem ViewModel
    val boulders by viewModel.boulders
    val vmLoading by viewModel.isLoading
    val errorMessage by viewModel.errorMessage
    val tickedBoulderIds by viewModel.tickedBoulderIds


    val lifecycleOwner = LocalLifecycleOwner.current

    // --- eigenes UI-Refreshing-Flag für mind. Sichtdauer des Spinners
    var isRefreshing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val MIN_SPINNER_MS = 800L

    val tickArea = 35.dp
    val tickSpacing = 3.dp
    var showFilter by remember { mutableStateOf(false) }

    val fbGrades = listOf(
        "3", "4", "5A", "5B", "5C",
        "6A", "6A+", "6B", "6B+", "6C", "6C+",
        "7A", "7A+", "7B", "7B+", "7C", "7C+",
        "8A", "8A+", "8B", "8B+", "8C", "8C+", "9A"
    )
    val gradeToIndex = remember { fbGrades.withIndex().associate { it.value to it.index } }

    fun gradeIdxOf(b: BoulderDTO) = gradeToIndex[b.difficulty]


    val rangeSaver = Saver<ClosedFloatingPointRange<Float>, List<Float>>(
        save = { listOf(it.start, it.endInclusive) },
        restore = { (s, e) -> s..e }
    )
    var sliderRange by rememberSaveable(stateSaver = rangeSaver) {
        mutableStateOf(0f..fbGrades.lastIndex.toFloat())
    }
    val startIndex = sliderRange.start.roundToInt().coerceIn(0, fbGrades.lastIndex)
    val endIndex = sliderRange.endInclusive.roundToInt().coerceIn(0, fbGrades.lastIndex)

    var excludeTicked by rememberSaveable { mutableStateOf(false) }


    var sortKey by rememberSaveable { mutableStateOf(SortKey.GRADE) } // Standard: Schwierigkeit
    var sortAscending by rememberSaveable { mutableStateOf(true) }


// Primär: welches Kriterium zuerst?
    var primaryKey by rememberSaveable { mutableStateOf(SortKey.GRADE) }

    // Richtung je Kriterium:
    var gradeAsc by rememberSaveable { mutableStateOf(true) }
    var starsAsc by rememberSaveable { mutableStateOf(true) }

    val tickStars by viewModel.tickStars

    fun starsOf(b: BoulderDTO): Int? = b.id?.let { tickStars[it] }


    val filteredBoulders = remember(
        boulders,
        startIndex,
        endIndex,
        tickedBoulderIds,
        excludeTicked,
        primaryKey,
        gradeAsc,
        starsAsc
    ) {
        val filtered = boulders.filter { b ->
            val idx = gradeIdxOf(b)
            val inRange = idx != null && idx in startIndex..endIndex
            if (!inRange) return@filter false

            if (excludeTicked && b.id != null && tickedBoulderIds.contains(b.id)) return@filter false

            true

        }

        // --- Einzelvergleich je Kriterium ---
        val cmpGrade = Comparator<BoulderDTO> { a, b ->
            val ia = gradeIdxOf(a) ?: Int.MAX_VALUE
            val ib = gradeIdxOf(b) ?: Int.MAX_VALUE
            val base = ia.compareTo(ib)
            if (gradeAsc) base else -base
        }


        // Sterne nach COMMUNITY-Durchschnitt (avgStars) sortieren; nulls last
        val cmpStars = Comparator<BoulderDTO> { a, b ->
            val sa = a.avgStars   // Double?
            val sb = b.avgStars   // Double?
            when {
                sa == null && sb == null -> 0
                sa == null -> 1
                sb == null -> -1
                else -> {
                    val base = sa.compareTo(sb)
                    if (starsAsc) base else -base
                }
            }
        }

        // Sekundärschlüssel ist das jeweils andere Kriterium
        val secondaryKey = if (primaryKey == SortKey.GRADE) SortKey.STARS else SortKey.GRADE
        val primaryCmp = if (primaryKey == SortKey.GRADE) cmpGrade else cmpStars
        val secondaryCmp = if (secondaryKey == SortKey.GRADE) cmpGrade else cmpStars

        filtered.sortedWith(primaryCmp.then(secondaryCmp).then(compareBy { it.name }))
    }

    // --- Pull-to-Refresh
    val refreshState = rememberPullRefreshState(
        refreshing = isRefreshing, // einziges Flag -> sorgt für drehende Animation
        onRefresh = {
            scope.launch {
                isRefreshing = true
                val t0 = System.currentTimeMillis()

                viewModel.load(context, spraywallId)
                viewModel.loadTickedBoulders(context)

                // warte bis VM fertig geladen hat
                while (viewModel.isLoading.value) {
                    delay(32)
                }

                // Mindestdauer sicherstellen
                val elapsed = System.currentTimeMillis() - t0
                if (elapsed < MIN_SPINNER_MS) delay(MIN_SPINNER_MS - elapsed)

                isRefreshing = false
            }
        }
    )

    val barColor = colorResource(id = R.color.hold_type_bar)


    // Daten laden
    LaunchedEffect(spraywallId) {
        viewModel.initRepository(context)
        viewModel.load(context, spraywallId)
        viewModel.loadTickedBoulders(context)
    }

    val screenBg = Brush.verticalGradient(
        colors = listOf(Color(0xFF53535B), Color(0xFF767981), Color(0xFFA8ABB2))
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
                    title = {
                        Text(
                            text = spraywallName,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = barColor,
                        scrolledContainerColor = barColor,
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White,
                        actionIconContentColor = Color.White
                    ),
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Zurück")
                        }
                    },
                    actions = {
                        val online = isOnline(context)
                        IconButton(
                            onClick = { showFilter = true },
                            enabled = online,
                            modifier = Modifier.alpha(if (online) 1f else 0.4f)
                        ) {
                            Icon(Icons.Default.FilterList, contentDescription = "Filter")
                        }
                    }
                )
            },
            bottomBar = { BottomNavigationBar(navController) }
        ) { innerPadding ->

            // Pull-to-Refresh muss am Container hängen
            Box(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .pullRefresh(refreshState)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    if (vmLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = colorResource(R.color.button_normal))
                        }
                        Spacer(Modifier.height(12.dp))
                    }

                    val listToShow = filteredBoulders


                    // Hinweis-/Fehlermeldungen aus dem ViewModel
                    errorMessage?.let {
                        Text("Hinweis: $it", color = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.height(8.dp))
                    }

                    if (listToShow.isEmpty() && !vmLoading) {
                        EmptyBouldersState()
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(listToShow) { boulder ->


                                // ⬇️ direkt in deinem items(listToShow) { boulder -> ... } einsetzen
                                val id = boulder.id ?: return@items
                                val isTicked = tickedBoulderIds.contains(id)
                                val accent = colorResource(R.color.button_normal)
                                val visibleIds: List<String> = listToShow.mapNotNull { it.id }

                                var menuOpen by remember { mutableStateOf(false) }

// Ownership/Superuser
                                val tokenStore = remember {
                                    com.example.sprayconnectapp.util.TokenStore.create(context)
                                }
                                val canDelete = remember(boulder.createdBy) {
                                    tokenStore.isSuperUser() || tokenStore.getUserId() == boulder.createdBy
                                }

                                val openBoulder: () -> Unit = {
                                    navController.currentBackStackEntry?.savedStateHandle
                                        ?.set("visibleIds", ArrayList(visibleIds))
                                    val encoded = Uri.encode(imageUri ?: "")
                                    navController.navigate("view_boulder/$id/$spraywallId?src=list&imageUri=$encoded")
                                }

                                Box { // Anker für das Dropdown
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                            .combinedClickable(
                                                onClick = { openBoulder() },
                                                onLongClick = { menuOpen = true }
                                            ),
                                        shape = RoundedCornerShape(12.dp),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color.White)
                                    ) {
                                        Column(
                                            Modifier.padding(
                                                horizontal = 12.dp,
                                                vertical = 6.dp
                                            )
                                        ) {
                                            Row(
                                                Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(
                                                    Modifier
                                                        .weight(1f)
                                                        .padding(end = tickSpacing + tickArea)
                                                ) {
                                                    Text(
                                                        boulder.name,
                                                        style = MaterialTheme.typography.titleMedium,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis,
                                                        softWrap = false
                                                    )
                                                    Spacer(Modifier.height(3.dp))
                                                    Text(
                                                        "Schwierigkeit: ${boulder.difficulty}",
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        color = Color(0xFF000000)
                                                    )

                                                    val avgRounded: Int? = boulder.avgStars
                                                        ?.takeIf { it.isFinite() && it > 0.0 }
                                                        ?.roundToInt()
                                                        ?.coerceIn(0, 5)

                                                    Spacer(Modifier.height(2.dp))
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        TinyStars(avgRounded ?: 0)
                                                        Text(
                                                            "(${boulder.starsCount ?: 0})",
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = Color.Gray
                                                        )
                                                    }
                                                }

                                                Spacer(Modifier.width(tickSpacing))

                                                Box(
                                                    Modifier.size(tickArea),
                                                    contentAlignment = Alignment.Center
                                                ) {
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

                                    // Kontextmenü (Long-Press)
                                    DropdownMenu(
                                        expanded = menuOpen,
                                        onDismissRequest = { menuOpen = false },
                                        containerColor = Color.White,          // <-- macht die "Dropdown-Card" weiß
                                        tonalElevation = 6.dp                  // optional
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("Öffnen") },
                                            onClick = { menuOpen = false; openBoulder() },
                                            leadingIcon = {
                                                Icon(
                                                    Icons.Default.ArrowUpward,
                                                    null,
                                                    tint = Color(0xFF000000)
                                                )
                                            },
                                            colors = MenuDefaults.itemColors(       // optional: Text/Icon-Farben setzen
                                                textColor = Color(0xFF000000),
                                                leadingIconColor = Color(0xFF000000)
                                            )
                                        )

                                        if (canDelete) {
                                            DropdownMenuItem(
                                                text = { Text("Löschen") },
                                                onClick = {
                                                    menuOpen = false
                                                    boulderToDelete = id
                                                    showDeleteDialog = true
                                                },
                                                colors = MenuDefaults.itemColors(       // optional: Text/Icon-Farben setzen
                                                    textColor = Color(0xFF000000),
                                                    leadingIconColor = Color(0xFF000000)
                                                ),
                                                leadingIcon = {
                                                    Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFD32F2F))
                                                }
                                            )
                                        }

                                    }
                                }
                            }

                        }
                    }
                }


            }

            // Sichtbarer Pull-to-Refresh-Indikator (dreht, solange isRefreshing=true)
            PullRefreshIndicator(
                refreshing = isRefreshing,
                state = refreshState,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 8.dp)
            )

            // --- Filter-Dialog
            if (showFilter) {
                AlertDialog(
                    onDismissRequest = { showFilter = false },
                    containerColor = Color(0xFFE5E5E5), // Hellgrauer Hintergrund
                    textContentColor = Color(0xFF000000),
                    confirmButton = {
                        TextButton(onClick = { showFilter = false }) {
                            Text(
                                "Fertig",
                                color = colorResource(R.color.button_normal),
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            sliderRange = 0f..fbGrades.lastIndex.toFloat()
                            excludeTicked = false
                            showFilter = false
                            primaryKey = SortKey.GRADE
                            gradeAsc = true
                            starsAsc = true
                        }) {
                            Text(
                                "Zurücksetzen",
                                color = Color(0xFF000000), // Schwarz
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    },
                    title = {
                        Text(
                            "Filter-Optionen:",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            color = Color(0xFF000000) // Schwarz
                        )
                    },
                    text = {
                        Column {
                            Spacer(Modifier.height(8.dp))

                            SortRow(
                                title = "Schwierigkeit",
                                isPrimary = primaryKey == SortKey.GRADE,
                                ascSelected = gradeAsc,
                                onSetPrimary = { primaryKey = SortKey.GRADE },
                                onAsc = { gradeAsc = true },
                                onDesc = { gradeAsc = false }
                            )

                            SortRow(
                                title = "Sterne",
                                isPrimary = primaryKey == SortKey.STARS,
                                ascSelected = starsAsc,
                                onSetPrimary = { primaryKey = SortKey.STARS },
                                onAsc = { starsAsc = true },
                                onDesc = { starsAsc = false }
                            )

                            Spacer(Modifier.height(19.dp))

                            Text(
                                " ${fbGrades[startIndex]} - ${fbGrades[endIndex]}",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center,
                                color = Color(0xFF000000)
                            )

                            Spacer(Modifier.height(2.dp))

                            RangeSlider(
                                value = sliderRange,
                                onValueChange = { r ->
                                    val s = r.start.roundToInt().coerceIn(0, fbGrades.lastIndex)
                                    val e =
                                        r.endInclusive.roundToInt().coerceIn(0, fbGrades.lastIndex)
                                    val (sIdx, eIdx) = if (s <= e) s to e else e to s
                                    sliderRange = sIdx.toFloat()..eIdx.toFloat()
                                },
                                valueRange = 0f..fbGrades.lastIndex.toFloat(),
                                steps = fbGrades.size - 2,
                                colors = SliderDefaults.colors(
                                    thumbColor = colorResource(R.color.button_normal),
                                    activeTrackColor = colorResource(R.color.button_normal),
                                    inactiveTrackColor = colorResource(R.color.button_normal).copy(
                                        alpha = 0.3f
                                    ),
                                    activeTickColor = Color.White,
                                    inactiveTickColor = Color.Gray
                                )
                            )

                            Spacer(Modifier.height(16.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    "Exclude my repeats:",
                                    modifier = Modifier.weight(1f),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color(0xFF000000)
                                )
                                Switch(
                                    checked = excludeTicked,
                                    onCheckedChange = { excludeTicked = it },
                                    colors = SwitchDefaults.colors(
                                        checkedTrackColor = colorResource(R.color.button_normal),
                                        uncheckedTrackColor = colorResource(R.color.button_normal).copy(
                                            alpha = 0.35f
                                        ),
                                        uncheckedThumbColor = Color.White,
                                        checkedBorderColor = Color.Transparent,
                                        uncheckedBorderColor = Color.Transparent
                                    )
                                )
                            }
                        }
                    }
                )
            }
            if (showDeleteDialog && boulderToDelete != null) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                shape = RoundedCornerShape(24.dp),
                containerColor = Color.White.copy(alpha = 0.95f),
                tonalElevation = 6.dp,

                title = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Boulder löschen?",
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.Black,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .width(70.dp)
                                .height(4.dp)
                                .background(colorResource(R.color.button_normal), RoundedCornerShape(2.dp))
                        )
                    }
                },

                text = {
                    Text(
                        "Dieser Vorgang kann nicht rückgängig gemacht werden.",
                        color = Color.Black,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                },

                confirmButton = {
                    TextButton(
                        onClick = {
                            val id = boulderToDelete
                            if (id != null) {
                                showDeleteDialog = false
                                viewModel.deleteBoulder(context, id) { ok ->
                                    if (ok) {
                                        Toast.makeText(context, "Boulder gelöscht", Toast.LENGTH_SHORT).show()
                                        viewModel.load(context, spraywallId)
                                        viewModel.loadTickedBoulders(context)
                                    } else {
                                        Toast.makeText(context, "Fehler beim Löschen", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = colorResource(R.color.button_normal)
                        )
                    ) {
                        Text("Löschen")
                    }
                },

                dismissButton = {
                    TextButton(
                        onClick = { showDeleteDialog = false },
                        colors = ButtonDefaults.textButtonColors(contentColor = Color.Black)
                    ) {
                        Text("Abbrechen")
                    }
                }
            )
        }


        }
    }
}


@Composable
private fun EmptyBouldersState() {
    Box(
        modifier = Modifier
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
                "Keine Boulder gefunden",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White
            )
            Text(
                "Passe die Filter-Optionen an oder erstelle einen neuen Boulder.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.85f),
                textAlign = TextAlign.Center
            )
        }
    }
}


@Composable
private fun TinyStars(ratingRounded: Int, modifier: Modifier = Modifier, max: Int = 5) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        repeat(max) { i ->
            Icon(
                imageVector = if (i < ratingRounded) Icons.Filled.Star else Icons.Outlined.Star,
                contentDescription = null,
                tint = if (i < ratingRounded) Color(0xFFFFC107) else Color(0xFFBDBDBD),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}


@Composable
fun SortRow(
    title: String,
    isPrimary: Boolean,
    ascSelected: Boolean,
    onSetPrimary: () -> Unit,
    onAsc: () -> Unit,
    onDesc: () -> Unit
) {
    val accent = colorResource(R.color.button_normal)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Titel: klickbar, um Primärsortierung zu setzen
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = if (isPrimary) accent else Color(0xFF000000), // Schwarz wenn inaktiv
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp)
                .clickable { onSetPrimary() }
        )

        // Pfeile ↑ / ↓ — Schwarz, App-Farbe wenn aktiv
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            IconButton(onClick = onAsc) {
                Icon(
                    imageVector = Icons.Filled.ArrowUpward,
                    contentDescription = "Aufsteigend",
                    tint = if (ascSelected) accent else Color(0xFF000000)
                )
            }
            IconButton(onClick = onDesc) {
                Icon(
                    imageVector = Icons.Filled.ArrowDownward,
                    contentDescription = "Absteigend",
                    tint = if (!ascSelected) accent else Color(0xFF000000)
                )
            }
        }
    }
}



