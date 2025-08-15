package com.example.sprayconnectapp.ui.screens.BoulderList

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.ui.graphics.Color



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

    val boulders by viewModel.boulders
    val isLoading by viewModel.isLoading
    val errorMessage by viewModel.errorMessage


    LaunchedEffect(spraywallId) {
        viewModel.initRepository(context)
        viewModel.load(context, spraywallId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Boulder – $spraywallName") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Zurück")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    val encodedUri = Uri.encode(imageUri)
                    navController.navigate(
                        "create_boulder/$spraywallId?imageUri=$encodedUri&mode=create"
                    )
                },
                containerColor = Color(0xFF26C6DA),
                contentColor = Color.White,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 8.dp,
                    pressedElevation = 12.dp
                )
            ) {
                Icon(Icons.Default.Add, contentDescription = "Neuen Boulder hinzufügen")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator()
            } else {
                if (errorMessage != null) {
                    Text("Hinweis: $errorMessage", color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(8.dp))
                }

                if (boulders.isEmpty()) {
                    Text("Keine Boulder gefunden.")
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(boulders) { boulder ->
                            Card(
                                onClick = {
                                    val id = boulder.id ?: return@Card
                                    val encoded = Uri.encode(imageUri ?: "")
                                    navController.navigate("view_boulder/$id/$spraywallId/$encoded")
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                shape = RoundedCornerShape(12.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = boulder.name,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Schwierigkeit: ${boulder.difficulty}",
                                        style = MaterialTheme.typography.bodyMedium
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
