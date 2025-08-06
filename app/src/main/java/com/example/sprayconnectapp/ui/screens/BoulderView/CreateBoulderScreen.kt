package com.example.sprayconnectapp.ui.screens.BoulderView

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sprayconnectapp.R
import com.example.sprayconnectapp.data.dto.HoldType


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateBoulderScreen(
    viewModel: CreateBoulderViewModel = viewModel(),
    onSave: () -> Unit = {},
    onBack: () -> Unit = {}

) {
    val uistate by viewModel.uiState
    val context = LocalContext.current



    var showDialog by remember { mutableStateOf(false) }
    var boulderName by remember { mutableStateOf("") }
    var boulderDifficulty by remember { mutableStateOf("") }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Boulder erstellen") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Zurück")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Check, contentDescription = "Speichern")
            }

        }
    ) { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Spraywall-Bild - später vom server laden momentan lokal
            Image(
                painter = painterResource(id = R.drawable.spray1),
                contentDescription = "Spraywall",
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures(onLongPress = { offset: Offset ->
                            val x = offset.x
                            val y = offset.y
                            viewModel.addHold(x, y)
                        })
                    },
                contentScale = ContentScale.Crop
            )

            // Holds zeichnen
            Box {
                uistate.holds.forEach { hold ->
                    val color = HoldType.valueOf(hold.type).color
                    Box(
                        modifier = Modifier
                            .offset {
                                IntOffset(hold.x.toInt(), hold.y.toInt())
                            }
                            .size(24.dp)
                            .background(color, CircleShape)
                            .border(2.dp, color, CircleShape)
                    )
                }
            }

            // Farb-Auswahl unten für Kreise
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                HoldType.entries.forEach { type ->
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .background(color = type.color, shape = CircleShape)
                            .border(
                                width = if (type == uistate.selectedType) 4.dp else 2.dp,
                                color = if (type == uistate.selectedType) Color.Black else Color.LightGray,
                                shape = CircleShape
                            )
                            .clickable { viewModel.selectHoldType(type) }
                    )
                }
            }


            //Dialogfenster wenn User Boulder speichern will
            if (showDialog) {
                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    confirmButton = {
                        TextButton(onClick = {
                            viewModel.saveBoulder(
                                context = context,
                                name = boulderName,
                                difficulty = boulderDifficulty,
                                spraywallId = "bf484068-c885-4737-aed3-bdb49741c645" // später dynamisch machen
                            )
                            showDialog = false
                            onSave()
                        }) {
                            Text("Speichern")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDialog = false }) {
                            Text("Abbrechen")
                        }
                    },
                    title = { Text("Boulder speichern") },
                    text = {
                        Column {
                            OutlinedTextField(
                                value = boulderName,
                                onValueChange = { boulderName = it },
                                label = { Text("Name") }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = boulderDifficulty,
                                onValueChange = { boulderDifficulty = it },
                                label = { Text("Schwierigkeit") }
                            )
                        }
                    }
                )
            }


        }
    }
}