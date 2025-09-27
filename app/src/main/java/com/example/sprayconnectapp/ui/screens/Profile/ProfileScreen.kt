package com.example.sprayconnectapp.ui.screens.Profile

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.compose.foundation.background
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

                            BoulderListCard(title = "Meine Boulder", boulders = boulders, navController = navController, source = "mine")

                            Spacer(Modifier.height(17.dp))

                            BoulderListCard(title = "Getickte Boulder", boulders = ticked, navController = navController, source = "ticked")

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
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoulderCard(boulder: BoulderDTO, onClick: (() -> Unit)? = null) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(4.dp),
        onClick = {
             onClick?.invoke()
        }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(boulder.name, style = MaterialTheme.typography.titleMedium)
            Text("Schwierigkeit: ${boulder.difficulty}", style = MaterialTheme.typography.bodyMedium)
        }
    }
}


/**
 * Karte mit einer Liste von Bouldern (scrollbar, begrenzte Höhe).
 * - Erzeugt bei Klick die Route zur Detailansicht
 * - Wenn lokales Bild gefunden wird, wird dessen URI als Query-Arg mitgegeben
 */


@Composable
fun BoulderListCard( title: String, boulders: List<BoulderDTO>, source: String, navController: NavController, maxHeight: Dp = 240.dp) {
    val context = LocalContext.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp).fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(title, style = MaterialTheme.typography.headlineSmall)
            Divider()

            if (boulders.isEmpty()) {
                Text("Keine Einträge gefunden.")
            } else {

                // Scrollbare Liste innerhalb der Karte
                LazyColumn(modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = maxHeight),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 4.dp)) {

                    // key für feste identität statt nur position - kann sich sonst verändern bei Änderungen
                    items(boulders,  key = { it.id ?: "fallback-${it.spraywallId}-${it.createdAt}" } ) { boulder ->
                        BoulderCard(boulder) {

                            // OnClick eines Boulder-Items -> zur Detailansicht navigieren

                            // 1) Preview-URL -> lokalen Dateinamen bestimmen
                            val preview = (boulder.spraywallImageUrl ?: "").trim()
                            val token = Regex("/s/([^/]+)/").find(preview)?.groupValues?.get(1)

                            // Basis-Route mit leerem imageUri-Segment am Ende
                            val base = "view_boulder/${boulder.id}/${boulder.spraywallId}"

                            // Wenn wir keine valide Preview haben, brech ab (kein Download hier!)
                            if (preview.isEmpty() || token.isNullOrEmpty()) {

                                Toast.makeText(context, "Kein lokales Bild gefunden", Toast.LENGTH_SHORT).show()
                                navController.navigate("$base?src=$source")
                                return@BoulderCard
                            }

                            // 2) Lokale Datei wie in SpraywallDetail benennen und nachschauen
                            val outName = localOutputNameFromPreview(preview, token)
                            val file = getPrivateImageFileByName(context, outName)


                            // versucht lokal vorhandene BildUri zu bauen für den ZielScreen
                            val encodedImage = if (!preview.isNullOrEmpty() && !token.isNullOrEmpty()){
                                val outName = localOutputNameFromPreview(preview, token) // Dateiname ableiten
                                val file = getPrivateImageFileByName(context, outName) //Datei Object im App Speicher ermitteln
                                // falls datei existiert in uri umwandeln
                                if (file.exists()) Uri.encode(Uri.fromFile(file).toString()) else ""
                            }  else ""


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
        }
    }
}

@Composable
fun ProfileUpdateCard() {
    val ctx = LocalContext.current
    var latest by remember { mutableStateOf(UpdatePrefs.readLatest(ctx)) } // Triple<Int, String, Pair<url, log>?>?
    var checking by remember { mutableStateOf(false) }
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

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (newer) {
                        Button(onClick = {
                            val req = DownloadManager.Request(Uri.parse(url))
                                .setTitle("SprayConnect Update")
                                .setDescription("Neue Version wird heruntergeladen…")
                                .setNotificationVisibility(
                                    DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
                                )
                                // legt die Datei im öffentlichen Download-Ordner ab:
                                .setDestinationInExternalPublicDir(
                                    Environment.DIRECTORY_DOWNLOADS,
                                    "sprayconnect-latest.apk"
                                )

                            val dm = ctx.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                            dm.enqueue(req)

                            Toast.makeText(ctx, "Download gestartet (siehe Benachrichtigung)", Toast.LENGTH_SHORT).show()
                        }) {
                            Text("Zur APK")
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





