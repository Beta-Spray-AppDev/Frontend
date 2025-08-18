package com.example.sprayconnectapp.ui.screens.Profile

import android.net.Uri
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
                    title = { Text("Mein Profil") },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }, ) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Zurück")
                        }
                    },
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
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = colorResource(R.color.button_normal))
                            }
                        }

                        error != null -> {
                            Text("Fehler: $error", color = MaterialTheme.colorScheme.error)
                        }

                        profile != null -> {
                            ProfileCard(profile = profile!!, navController = navController)
                            Spacer(modifier = Modifier.height(17.dp))

                            BoulderListCard(title = "Meine Boulder", boulders = boulders, navController = navController, source = "mine")

                            Spacer(Modifier.height(17.dp))

                            BoulderListCard(title = "Getickte Boulder", boulders = ticked, navController = navController, source = "ticked")

                        }

                        else -> {
                            Text("Keine Profildaten vorhanden.")
                        }
                    }
                }
            }
        }



    }

}


@Composable
fun ProfileInfoRow(label: String, value: String) {
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
                LazyColumn(modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = maxHeight),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 4.dp)) {
                    items(boulders,  key = { it.id ?: "fallback-${it.spraywallId}-${it.createdAt}" } ) { boulder ->
                        BoulderCard(boulder) {
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

                            val encodedImage = if (!preview.isNullOrEmpty() && !token.isNullOrEmpty()){
                                val outName = localOutputNameFromPreview(preview, token)
                                val file = getPrivateImageFileByName(context, outName)
                                if (file.exists()) Uri.encode(Uri.fromFile(file).toString()) else ""
                            }  else ""

                            val route = buildString {
                                append("$base?src=$source")
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




