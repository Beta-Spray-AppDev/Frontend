package com.example.sprayconnectapp.ui.screens.BoulderList

import android.net.Uri
import kotlin.math.roundToInt
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.FilterList
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.sprayconnectapp.R
import com.example.sprayconnectapp.ui.screens.BottomNavigationBar
import androidx.compose.ui.text.style.TextOverflow
import com.example.sprayconnectapp.ui.screens.isOnline
import kotlin.math.roundToInt

import androidx.compose.material.icons.outlined.ImageSearch
import androidx.compose.material.icons.outlined.Star
import androidx.compose.ui.text.style.TextAlign
import com.example.sprayconnectapp.data.dto.BoulderDTO


enum class SortKey { GRADE, STARS }


@OptIn(ExperimentalMaterial3Api::class)
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

    var minStars by rememberSaveable { mutableStateOf(0) }

    // UI-State aus dem ViewModel
    val boulders by viewModel.boulders
    val isLoading by viewModel.isLoading
    val errorMessage by viewModel.errorMessage
    val tickedBoulderIds by viewModel.tickedBoulderIds

    val tickArea = 35.dp      // Icongröße
    val tickSpacing = 3.dp   // Abstand vor dem Icon

    var showFilter by remember { mutableStateOf(false) }

    val fbGrades = listOf(
        "3", "4", "5A", "5B", "5C",
        "6A", "6A+", "6B", "6B+", "6C", "6C+",
        "7A", "7A+", "7B", "7B+", "7C", "7C+",
        "8A", "8A+", "8B", "8B+", "8C", "8C+", "9A"
    )

    val rangeSaver = Saver<ClosedFloatingPointRange<Float>, List<Float>>(
        save = { listOf(it.start, it.endInclusive)  },
        restore = { (s, e) -> s..e }
    )

    var sliderRange by rememberSaveable(stateSaver = rangeSaver) {
        mutableStateOf(0f..fbGrades.lastIndex.toFloat())
    }

    val startIndex = sliderRange.start.roundToInt().coerceIn(0, fbGrades.lastIndex)
    val endIndex   = sliderRange.endInclusive.roundToInt().coerceIn(0, fbGrades.lastIndex)


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

    val gradeToIndex = remember { fbGrades.withIndex().associate { it.value to it.index } }
    fun gradeIdxOf(b: BoulderDTO) = gradeToIndex[b.difficulty]





    val filteredBoulders = remember(boulders, startIndex, endIndex,tickedBoulderIds, excludeTicked, primaryKey, gradeAsc, starsAsc) {
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
        val primaryCmp   = if (primaryKey == SortKey.GRADE) cmpGrade else cmpStars
        val secondaryCmp = if (secondaryKey == SortKey.GRADE) cmpGrade else cmpStars

        filtered.sortedWith(primaryCmp.then(secondaryCmp).then(compareBy { it.name }))
    }












    val BarColor = colorResource(id = R.color.hold_type_bar)

    // Daten laden
    LaunchedEffect(spraywallId) {
        viewModel.initRepository(context)
        viewModel.load(context, spraywallId)
        viewModel.loadTickedBoulders(context)


        
    }

    // Hintergrund mit Farbverlauf
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
                    title = {
                        Text(
                            text = spraywallName,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = BarColor,
                        scrolledContainerColor = BarColor,
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White,
                        actionIconContentColor = Color.White
                    ),
                    // Zurück zur vorherigen Route
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Zurück")
                        }
                    },

                    //Plus-Button zum Erstellen eines neuen Boulders
                    actions = {
                        val online = isOnline(context)

                        IconButton(
                            onClick = {
                                showFilter = true
                            },
                            enabled = online,
                            modifier = Modifier.alpha(if (online) 1f else 0.4f)
                        ) {
                            Icon(Icons.Default.FilterList, contentDescription = "Boulder hinzufügen")
                        }
                    }
                )
            },
            // App-weite Bottom-Navigation
            bottomBar = { BottomNavigationBar(navController) }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(16.dp)
            ) {
                if (isLoading) {
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

                if (listToShow.isEmpty() && !isLoading) {
                    EmptyBouldersState()
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(listToShow) { boulder ->

                            val isTicked = boulder.id != null && tickedBoulderIds.contains(boulder.id)

                            val accent = colorResource(R.color.button_normal)
                            val tickedBg = Color(0xFFE6FAF7)

                            Card(
                                // Detailansicht öffnen
                                onClick = {
                                    val id = boulder.id ?: return@Card
                                    val encoded = Uri.encode(imageUri ?: "")
                                    navController.navigate("view_boulder/$id/$spraywallId?src=list&imageUri=$encoded")
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                shape = RoundedCornerShape(12.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color.White//if (isTicked) Color(0xFFDFF5F5) else MaterialTheme.colorScheme.surface
                                )
                            ) {
                                Column(Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
                                    Row(
                                        Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(Modifier.weight(1f).padding(end = tickSpacing + tickArea)) {
                                            Text(boulder.name, style = MaterialTheme.typography.titleMedium,  maxLines = 1, overflow = TextOverflow.Ellipsis,     softWrap = false
                                            )
                                            Spacer(Modifier.height(3.dp))
                                            Text("Schwierigkeit: ${boulder.difficulty}", style = MaterialTheme.typography.bodyMedium)

                                            val avgRounded: Int? = boulder.avgStars
                                                ?.takeIf { it.isFinite() && it > 0.0 }
                                                ?.roundToInt()
                                                ?.coerceIn(1, 5)

                                            val myStars: Int? = starsOf(boulder)


                                            when {
                                                // 1) Community-Durchschnitt zeigen (mit Count)
                                                avgRounded != null -> {
                                                    Spacer(Modifier.height(2.dp))
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        TinyStars(avgRounded)
                                                        boulder.starsCount?.let { cnt ->
                                                            Spacer(Modifier.width(6.dp))
                                                            Text("($cnt)", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                                        }
                                                    }
                                                }

                                                // 2) sonst (wenn kein avg vorhanden) meine Sterne
                                                myStars != null -> {
                                                    Spacer(Modifier.height(2.dp))
                                                    TinyStars(myStars)
                                                }

                                                // 3) sonst gar nix
                                            }

                                        }

                                        // Fester Bereich für den Haken – immer gleich breit
                                        Spacer(Modifier.width(tickSpacing))


                                        // Fester Iconbereich – immer vorhanden
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
                    }
                }
            }



            val resetColor = Color(0xFFB3261E)



            // Dialog mit RangeSlider
            if (showFilter) {
                AlertDialog(
                    onDismissRequest = { showFilter = false },
                    confirmButton = {
                        TextButton(onClick = { showFilter = false }) {
                            Text(
                                "Fertig",
                                color = colorResource(R.color.button_normal),
                                style = MaterialTheme.typography.titleMedium
                            )                        }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            sliderRange = 0f..fbGrades.lastIndex.toFloat() // Reset
                            excludeTicked = false
                            showFilter = false
                            sortAscending = true
                        }) {
                            Text(
                                "Zurücksetzen",
                                color = Color.Black,
                                style = MaterialTheme.typography.titleMedium
                            )                        }
                    },
                    title = { Text("Filter-Optionen:", modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center) },
                    text = {
                        Column {

                            Spacer(Modifier.height(10.dp))
                            Text("Sortieren:",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center)
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
                                textAlign = TextAlign.Center
                            )
                            Spacer(Modifier.height(2.dp))
                            RangeSlider(
                                value = sliderRange,
                                onValueChange = { r ->
                                    val s = r.start.roundToInt().coerceIn(0, fbGrades.lastIndex)
                                    val e = r.endInclusive.roundToInt().coerceIn(0, fbGrades.lastIndex)
                                    val (sIdx, eIdx) = if (s <= e) s to e else e to s
                                    sliderRange = sIdx.toFloat()..eIdx.toFloat()
                                },
                                valueRange = 0f..fbGrades.lastIndex.toFloat(),
                                steps = fbGrades.size - 2,
                                colors = SliderDefaults.colors(
                                    thumbColor = colorResource(R.color.button_normal),
                                    activeTrackColor = colorResource(R.color.button_normal),
                                    inactiveTrackColor = colorResource(R.color.button_normal).copy(alpha = 0.3f),
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
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Switch(
                                    checked = excludeTicked,
                                    onCheckedChange = { excludeTicked = it },
                                    colors = SwitchDefaults.colors(
                                        checkedTrackColor = colorResource(R.color.button_normal),
                                        uncheckedTrackColor = colorResource(R.color.button_normal).copy(alpha = 0.35f),
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
private fun SortRow(
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
            color = if (isPrimary) accent else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp)
                .let { m ->
                    // ganze Titel-Zeile klickbar machen
                    Modifier.then(m).clickable { onSetPrimary() }
                }
        )

        // Pfeile: ↑ / ↓
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            IconButton(onClick = onAsc) {
                Icon(
                    imageVector = Icons.Filled.ArrowUpward,
                    contentDescription = "Aufsteigend",
                    tint = if (ascSelected) accent else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onDesc) {
                Icon(
                    imageVector = Icons.Filled.ArrowDownward,
                    contentDescription = "Absteigend",
                    tint = if (!ascSelected) accent else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}


