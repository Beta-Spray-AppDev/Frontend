package com.example.sprayconnectapp.ui.screens.home

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.sprayconnectapp.R
import com.example.sprayconnectapp.ui.screens.BottomNavigationBar
import com.example.sprayconnectapp.ui.screens.Profile.ProfileViewModel
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.ui.text.input.ImeAction
import com.example.sprayconnectapp.util.AppMeta

import com.example.sprayconnectapp.util.LatestRelease
import com.example.sprayconnectapp.util.UpdateChecker
import com.example.sprayconnectapp.BuildConfig


import com.example.sprayconnectapp.util.TokenStore




@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    val context = LocalContext.current
    val viewModel: HomeViewModel = viewModel()
    val profileViewModel: ProfileViewModel = viewModel()
    val profile by profileViewModel.profile.collectAsState()

    val sending by viewModel.feedbackSending
    val sendErr by viewModel.feedbackError
    val sendOk by viewModel.feedbackResult

    var showFeedbackDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.initRepository(context)
        viewModel.loadGyms(context)
        profileViewModel.loadProfile(context)
    }

    val store = TokenStore(context)
    val displayName = profile?.username ?: store.getUsername().orEmpty()


    val screenBg = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF53535B),
            Color(0xFF767981),
            Color(0xFFA8ABB2)
        )
    )
    val barColor = colorResource(id = R.color.hold_type_bar)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(screenBg)
    ) {

        UpdateNotice()


        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("SprayConnect") },
                    actions = {
                        // Immer verfügbar: Feedback
                        IconButton(
                            onClick = { showFeedbackDialog = true },
                            modifier = Modifier.focusProperties { canFocus = false }
                        ) {
                            Icon(Icons.Filled.RateReview, contentDescription = "Feedback geben")
                        }
                        // ➕ Neues Gym
                        IconButton(
                            onClick = { navController.navigate("addGym") },
                            modifier = Modifier.focusProperties { canFocus = false }
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Neues Gym anlegen")
                        }
                    },
                    navigationIcon = {
                        IconButton(
                            modifier = Modifier.focusProperties { canFocus = false },
                            onClick = {
                                viewModel.logout(context)
                                navController.navigate("login") {
                                    popUpTo("home") { inclusive = true }
                                }
                            }
                        ) {
                            Icon(
                                Icons.Default.ExitToApp,
                                contentDescription = "Logout",
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = barColor,
                        scrolledContainerColor = barColor,
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White,
                        actionIconContentColor = Color.White
                    ),
                )
            },
            bottomBar = { BottomNavigationBar(navController) }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(32.dp))
                Text(
                    text = "Hallo $displayName!",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White
                )
                Spacer(Modifier.height(32.dp))

                when {
                    viewModel.isLoading.value -> {
                    CircularProgressIndicator(color = colorResource(R.color.button_normal), modifier = Modifier.align(Alignment.CenterHorizontally))
                    }
                    viewModel.errorMessage.value != null -> {
                        Text(
                            "Fehler: ${viewModel.errorMessage.value}",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(viewModel.gyms.value) { gym ->
                                Card(
                                    onClick = {
                                        val id = gym.id.toString()
                                        val name = Uri.encode(gym.name)
                                        navController.navigate("spraywallDetail/$id/$name")
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(Modifier.padding(16.dp)) {
                                        Text(gym.name, style = MaterialTheme.typography.titleMedium)
                                        Text(gym.location, style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Dialog einhängen (nur wenn aktiv)
            if (showFeedbackDialog) {
                FeedbackDialog(
                    onDismiss = { showFeedbackDialog = false; viewModel.resetFeedbackState() },
                    onSubmit = { stars, text ->
                        val username = displayName.ifBlank { "anonym" }
                        val appVersion = BuildConfig.VERSION_NAME
                        val deviceInfo = AppMeta.deviceInfo()


                        val dto = com.example.sprayconnectapp.data.dto.feedback.CreateFeedbackDto(
                            stars = stars,
                            message = text.ifBlank { null },
                            username = username,
                            appVersion = appVersion,
                            deviceInfo = deviceInfo
                        )
                        viewModel.sendFeedback(dto)
                    }
                )

                when {
                    sending -> {
                        // optional: kleinen Hinweis/Loader zeigen
                    }
                    sendOk != null -> {
                        android.widget.Toast
                            .makeText(context, "Danke für dein Feedback!", android.widget.Toast.LENGTH_SHORT)
                            .show()
                        showFeedbackDialog = false
                        viewModel.resetFeedbackState()
                    }
                    sendErr != null -> {
                        android.widget.Toast
                            .makeText(context, "Fehler: $sendErr", android.widget.Toast.LENGTH_SHORT)
                            .show()
                        // Dialog offen lassen, damit Nutzer anpassen kann
                    }
                }
            }
        }
    }
}

/** Immer verfügbarer Feedback-Dialog (weißer Button, schwarze Schrift) */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedbackDialog(
    onDismiss: () -> Unit,
    onSubmit: (stars: Int, text: String) -> Unit,
    maxMessageLength: Int = 500
) {
    var stars by remember { mutableStateOf(0) }
    var text by remember { mutableStateOf("") }
    val isValid = stars > 0
    val remaining = maxMessageLength - text.length

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Wie gefällt dir die Beta?") },
        text = {
            Column {
                // Sterne-Auswahl
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    (1..5).forEach { i ->
                        IconButton(onClick = { stars = i }) {
                            Icon(
                                imageVector = if (i <= stars) Icons.Filled.Star else Icons.Outlined.Star,
                                contentDescription = "$i Sterne",
                                tint = if (i <= stars) Color(0xFFFFC107) else Color(0xFFBDBDBD)
                            )
                        }
                    }

                }

                Spacer(Modifier.height(8.dp))

                // Optionales Textfeld
                OutlinedTextField(
                    value = text,
                    onValueChange = { if (it.length <= maxMessageLength) text = it },
                    label = { Text("Dein Feedback (optional)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colorResource(R.color.button_normal),
                        unfocusedBorderColor = colorResource(R.color.button_normal),
                        focusedLabelColor = colorResource(R.color.button_normal),
                        cursorColor = colorResource(R.color.button_normal),
                        focusedTextColor = Color.Black,           // optional
                        unfocusedTextColor = Color.Black          // optional
                    ),
                    minLines = 3,
                    maxLines = 6,
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { if (isValid) onSubmit(stars, text.trim()) },
                enabled = isValid,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = colorResource(R.color.button_normal),
                    disabledContentColor = Color.LightGray
                )
            ) {
                Text("Senden")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Später", color = Color.Black) }
        }
    )
}


@Composable
fun CheckForUpdateDialog() {
    val context = LocalContext.current
    var latest by remember { mutableStateOf<LatestRelease?>(null) }
    var showDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val release = UpdateChecker.fetchLatest(BuildConfig.LATEST_JSON_URL)

        if (release != null &&
            release.versionCode > BuildConfig.VERSION_CODE &&
            !release.apkUrl.isNullOrBlank()
        ) {
            latest = release
            showDialog = true
        }
    }

    if (showDialog && latest != null) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Neue Version verfügbar!") },
            text = {
                Text("Version ${latest!!.versionName} ist verfügbar.\n\n${latest!!.changelog ?: ""}")
            },
            confirmButton = {
                TextButton(onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(latest!!.apkUrl))
                    context.startActivity(intent)
                    showDialog = false
                }) {
                    Text("Download starten")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Später")
                }
            }
        )
    }
}

@Composable
fun UpdateNotice() {
    val ctx = LocalContext.current
    var latest by remember { mutableStateOf<LatestRelease?>(null) }
    var show by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        Log.d("Update", "➡️ Starte Update-Check...")
        Log.d("Update", "➡️ URL = ${BuildConfig.LATEST_JSON_URL}")
        Log.d("Update", "➡️ Aktuelle App-Version = ${BuildConfig.VERSION_CODE}")

        val l = UpdateChecker.fetchLatest(BuildConfig.LATEST_JSON_URL)

        Log.d("Update", "➡️ Ergebnis vom Server: $l")

        if (l == null) {
            Log.e("Update", "❌ Kein gültiges JSON empfangen oder Fehler beim Laden")
            return@LaunchedEffect
        }

        val condCode = l.versionCode > BuildConfig.VERSION_CODE
        val condUrl = !l.apkUrl.isNullOrBlank()
        val seen = UpdatePrefs.wasSeen(ctx, l.versionCode)

        Log.d("Update", "➡️ condCode=$condCode condUrl=$condUrl seen=$seen")

        if (condCode && condUrl) {
            UpdatePrefs.saveLatest(ctx, l.versionCode, l.versionName, l.apkUrl!!, l.changelog)
            if (!seen) {
                latest = l
                show = true
                Log.d("Update", "✅ Zeige Update-Dialog für Version ${l.versionName}")
            } else {
                Log.d("Update", "ℹ️ Update wurde bereits gesehen, Dialog wird nicht erneut gezeigt")
            }
        } else {
            Log.d("Update", "ℹ️ Bedingungen nicht erfüllt – kein Update-Dialog")
        }
    }


    if (show && latest != null) {
        AlertDialog(
            onDismissRequest = {
                UpdatePrefs.markSeen(ctx, latest!!.versionCode)
                show = false
            },
            title = { Text("Neue Version ${latest!!.versionName}") },
            text = { Text(latest!!.changelog ?: "Es gibt ein Update.") },
            confirmButton = {
                TextButton(onClick = {
                    UpdatePrefs.markSeen(ctx, latest!!.versionCode)
                    ctx.startActivity(
                        Intent(Intent.ACTION_VIEW, Uri.parse(latest!!.apkUrl))
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    )
                    show = false
                }) { Text("Zur APK") }
            },
            dismissButton = {
                TextButton(onClick = {
                    UpdatePrefs.markSeen(ctx, latest!!.versionCode)
                    show = false
                }) { Text("Später") }
            }
        )
    }
}


