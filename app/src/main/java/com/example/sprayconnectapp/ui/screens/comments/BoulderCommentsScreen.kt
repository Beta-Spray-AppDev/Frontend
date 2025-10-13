// com.example.sprayconnectapp.ui.screens.comments/BoulderCommentsScreen.kt
package com.example.sprayconnectapp.ui.screens.comments

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.sprayconnectapp.R
import com.example.sprayconnectapp.data.dto.CommentDto
import java.text.DateFormat
import java.util.Date
import com.example.sprayconnectapp.util.Superusers
import com.example.sprayconnectapp.util.TokenStore



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoulderCommentsScreen(
    navController: NavController,
    boulderId: String,
    boulderName: String,
    vm: BoulderCommentsViewModel = viewModel(),
) {
    val ctx = LocalContext.current
    val comments by vm.comments
    val isLoading by vm.isLoading
    val error by vm.error
    val store = TokenStore.create(ctx)
    val currentUserId = store.getUserId()
    val isSuper = Superusers.isSuper(currentUserId)

    var toDelete by remember { mutableStateOf<CommentDto?>(null) }

    var showDialog by remember { mutableStateOf(false) }

    // Initial laden
    LaunchedEffect(boulderId) { vm.load(ctx, boulderId) }

    val barColor = colorResource(R.color.hold_type_bar)
    val screenBg = Brush.verticalGradient(
        colors = listOf(Color(0xFF53535B), Color(0xFF767981), Color(0xFFA8ABB2))
    )

    Box(Modifier.fillMaxSize().background(screenBg)) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text(boulderName.ifBlank { "Boulder" }) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Zurück")
                        }
                    },

                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = barColor,
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White,
                        actionIconContentColor = Color.White
                    )
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { showDialog = true },
                    containerColor = Color(0xFF7FBABF)
                ) { Icon(Icons.Default.RateReview, contentDescription = "Kommentieren") }
            }
        ) { inner ->
            Column(Modifier.padding(inner).fillMaxSize()) {

                // --- Stats-Bereich (Platzhalter für später) ---
                StatsHeader(
                    totalSends = null,          // TODO: später befüllen
                    avgRating = null,           // TODO: später befüllen
                    lastActivity = comments.maxOfOrNull { it.created }?.let { formatDate(it) }
                )

                Spacer(Modifier.height(8.dp))

                when {
                    isLoading -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = colorResource(R.color.button_normal))
                        }
                    }
                    error != null -> {
                        ErrorBox(message = error!!) { vm.refresh(ctx, boulderId) }
                    }
                    comments.isEmpty() -> {
                        EmptyBox(
                            text = "Noch keine Kommentare.\nSei der Erste und schreibe einen!"
                        )
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(16.dp)
                        ) {
                            items(comments, key = { it.id }) { c ->
                                val isOwner = currentUserId != null && c.userId.toString() == currentUserId
                                CommentCard(
                                    c = c,
                                    canLongDelete = isOwner || isSuper,
                                    onLongDelete = { toDelete = c }
                                )
                            }
                        }

                        // Bestätigungsdialog fürs Löschen
                        if (toDelete != null) {
                            AlertDialog(
                                onDismissRequest = { toDelete = null },
                                containerColor = Color(0xFFE5E5E5),       // Hellgrauer Hintergrund
                                textContentColor = Color(0xFF000000),     // Schwarzer Text
                                title = { Text("Kommentar löschen?", color = Color(0xFF000000)) },
                                text = { Text("Willst du diesen Kommentar wirklich löschen?", color = Color(0xFF000000)) },
                                confirmButton = {
                                    TextButton(
                                        onClick = {
                                            val id = toDelete!!.id.toString()
                                            vm.deleteComment(
                                                context = ctx,
                                                commentId = id,
                                                onSuccess = {
                                                    android.widget.Toast
                                                        .makeText(ctx, "Gelöscht", android.widget.Toast.LENGTH_SHORT)
                                                        .show()
                                                    toDelete = null
                                                    vm.refresh(ctx, boulderId)
                                                }
                                            )
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
                                        onClick = { toDelete = null },
                                        colors = ButtonDefaults.textButtonColors(
                                            contentColor = Color(0xFF000000)
                                        )
                                    ) {
                                        Text("Abbrechen")
                                    }
                                }
                            )

                            // optionales Feedback
                            when {
                                vm.deleting -> {
                                    // z. B. Loader anzeigen oder Snackbar
                                }
                                vm.deleteError != null -> android.widget.Toast
                                    .makeText(ctx, vm.deleteError, android.widget.Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }


                    }
                }
            }
        }

        if (showDialog) {
            CommentDialog(
                onDismiss = { showDialog = false },
                onSubmit = { text ->
                    vm.addComment(
                        context = ctx,
                        boulderId = boulderId,
                        text = text,
                        onSuccess = {
                            android.widget.Toast.makeText(ctx, "Kommentar gespeichert", android.widget.Toast.LENGTH_SHORT).show()
                            showDialog = false
                            vm.refresh(ctx, boulderId)
                        }
                    )
                }
            )


        }

    }
}

@Composable
private fun StatsHeader(
    totalSends: Int?,
    avgRating: Double?,
    lastActivity: String?
) {
    Row(
        Modifier.fillMaxWidth().padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(
            label = "Sends",
            value = totalSends?.toString() ?: "—",
            modifier = Modifier.weight(1f)
        )
        StatCard(
            label = "Ø Bewertung",
            value = avgRating?.let { String.format("%.1f", it) } ?: "—",
            modifier = Modifier.weight(1f)
        )
        StatCard(
            label = "Aktivität",
            value = lastActivity ?: "—",
            modifier = Modifier.weight(1.3f)
        )
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier, colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Column(Modifier.padding(12.dp)) {
            Text(label, color = Color(0xFF3F888F), style = MaterialTheme.typography.labelMedium)
            Spacer(Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
        }
    }
}

@Composable
private fun CommentCard(
    c: CommentDto,
    canLongDelete: Boolean = false,
    onLongDelete: () -> Unit = {}
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { /* optional: später Details */ },
                onLongClick = { if (canLongDelete) onLongDelete() }
            )
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(c.createdByUsername ?: c.userId.toString().take(8), fontWeight = FontWeight.SemiBold)
                Text(formatDate(c.created), style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            Text(c.content)
            if (canLongDelete) {
                Text(
                    "⟲ Lang drücken zum Löschen",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF888888)
                )
            }
        }
    }
}

@Composable
private fun ErrorBox(message: String, onRetry: () -> Unit) {
    Column(Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Fehler: $message", color = MaterialTheme.colorScheme.error)
        Spacer(Modifier.height(8.dp))
        TextButton(onClick = onRetry) { Text("Erneut laden", color = colorResource(R.color.button_normal)) }
    }
}

@Composable
private fun EmptyBox(text: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text, color = Color.White.copy(alpha = 0.9f))
    }
}

private fun formatDate(ms: Long): String {
    val df = DateFormat.getDateInstance()
    return df.format(Date(ms))
}
