package com.example.sprayconnectapp.ui.screens.GymDetail
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.sprayconnectapp.ui.BackButton




@Composable
fun GymDetailScreen(navController: NavController, gymName: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BackButton(navController)


        Spacer(modifier = Modifier.height(32.dp))

        Text(text = gymName, style = MaterialTheme.typography.headlineLarge)

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                // TODO: Boulde-Liste anzeigen
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Boulder anzeigen")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                // TODO: Spraywall-Liste anzeigen
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Spraywall auswählen")
        }
    }
}