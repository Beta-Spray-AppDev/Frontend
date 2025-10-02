package com.example.sprayconnectapp.ui.screens.BoulderList

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.FilterList
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
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material.icons.outlined.ImageSearch
import androidx.compose.ui.text.style.TextAlign


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

    // --- UI-State
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
    val gradeToIndex = remember { fbGrades.withIndex().associate { it.value to it.index } }

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
    var sortAscending by rememberSaveable { mutableStateOf(true) }

    val filteredBoulders = remember(boulders, startIndex, endIndex, tickedBoulderIds, excludeTicked, sortAscending) {
        val filtered = boulders.filter { b ->
            val idx = gradeToIndex[b.difficulty]
            val inRange = idx != null && idx in startIndex..endIndex
            if (!inRange) return@filter false
            if (!excludeTicked) return@filter true
            val id = b.id
            id == null || !tickedBoulderIds.contains(id)
        }
        val sorted = filtered.sortedBy { gradeToIndex[it.difficulty] ?: Int.MAX_VALUE }
        if (sortAscending) sorted else sorted.asReversed()
    }

    // --- Pull-to-Refresh (Material pullrefresh)

    val refreshState = rememberPullRefreshState(
        refreshing = isLoading,
        onRefresh = {
            viewModel.load(context, spraywallId)
            viewModel.loadTickedBoulders(context)
        }
    )

    val barColor = colorResource(id = R.color.hold_type_bar)

    // Initial laden
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

            // Hier passiert Pull-to-Refresh
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
                                Card(
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
                                    colors = CardDefaults.cardColors(containerColor = Color.White)
                                ) {
                                    Column(Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
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
                                                    style = MaterialTheme.typography.bodyMedium
                                                )
                                            }
                                            Spacer(Modifier.width(tickSpacing))
                                            Box(Modifier.size(tickArea), contentAlignment = Alignment.Center) {
                                                if (isTicked) {
                                                    Icon(
                                                        imageVector = Icons.Default.CheckCircle,
                                                        contentDescription = "Getickt",
                                                        tint = colorResource(R.color.button_normal)
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

                // Sichtbarer Indicator (Foundation/Material-PullRefresh)
                PullRefreshIndicator(
                    refreshing = isLoading,
                    state = refreshState,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 8.dp)
                )
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
                            Text("Sortierung nach Schwierigkeit:", style = MaterialTheme.typography.titleMedium,  modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center)
                            Spacer(Modifier.height(8.dp))


                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                FilterChip(
                                    selected = sortAscending,
                                    onClick = { sortAscending = true },
                                    label = { Text("Aufsteigend") },
                                    leadingIcon = {
                                        Icon(Icons.Filled.ArrowUpward, contentDescription = null)
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = colorResource(R.color.button_normal),
                                        selectedLabelColor = Color.White,
                                        selectedLeadingIconColor = Color.White
                                    )
                                )

                                FilterChip(
                                    selected = !sortAscending,
                                    onClick = { sortAscending = false },
                                    label = { Text("Absteigend") },
                                    leadingIcon = {
                                        Icon(Icons.Filled.ArrowDownward, contentDescription = null)
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = colorResource(R.color.button_normal),
                                        selectedLabelColor = Color.White,
                                        selectedLeadingIconColor = Color.White
                                    )
                                )
                            }

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

