package com.example.sprayconnectapp.ui.screens.register

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.sprayconnectapp.R

@Composable
fun RegisterScreen(navController: NavController, viewModel: RegisterViewModel = viewModel()) {


    // Farbverlauf
    val screenBg = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF2E2E2E),
            Color(0xFF4D4D4D)
        )
    )


    val focusManager = LocalFocusManager.current // zum Keyboard schließen
    val context = LocalContext.current


    val headerHeight = 240.dp // höhe des oberen Bereichs
    val cardOverlap  = 40.dp // Überlappung der Card nach unten





    Box(modifier = Modifier
        .fillMaxSize()
        .background(screenBg)
    ){

        // Header
        Surface(
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
            color = colorResource(id = R.color.button_normal),
            shape = RoundedCornerShape(bottomStart = 36.dp, bottomEnd = 36.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(380.dp)
                .align(Alignment.TopCenter)
        ){

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 48.dp) // Abstand von oben
            ){


                // App Logo
                Image(
                    painter = painterResource(R.drawable.start),
                    contentDescription = null,
                    modifier = Modifier
                        .size(90.dp)
                )
                Spacer(Modifier.height(8.dp))

                // App Name
                Text(
                    text = "SprayConnect",
                    color = Color.Black,
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center
                )
            }

        }

        // Card mit Formular
        Card(
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFDFDFE)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .align(Alignment.TopCenter)
                .offset(y = headerHeight - cardOverlap) // Überlappung nach unten
        ){

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ){

                // Titel
                Text(
                    "Registrieren",
                    style = MaterialTheme.typography.headlineMedium,
                    color = colorResource(id = R.color.button_normal)
                )

                Spacer(Modifier.height(16.dp))

                // Benutzername
                OutlinedTextField(
                    value = viewModel.username,
                    onValueChange = viewModel::onUsernameChange,
                    label = { Text("Benutzername") },
                    leadingIcon = { Icon(Icons.Filled.Person, null) },
                    singleLine = true,
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.fillMaxWidth(),
                    isError = viewModel.usernameError != null,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF00796B),
                        cursorColor = Color(0xFF00796B),
                        focusedLabelColor = Color(0xFF00796B)
                    ),
                    supportingText = {
                        // Fehlermeldung falls vorhanden
                        if (viewModel.usernameError != null) {
                            Text(viewModel.usernameError!!)
                        }
                    }
                )

                Spacer(Modifier.height(12.dp))



                // E-Mail
                OutlinedTextField(
                    value = viewModel.email,
                    onValueChange = viewModel::onEmailChange,
                    label = { Text("E-Mail") },
                    leadingIcon = { Icon(Icons.Filled.Email, null) },
                    singleLine = true,
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.fillMaxWidth(),
                    isError = viewModel.emailError != null,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF00796B),
                        cursorColor = Color(0xFF00796B),
                        focusedLabelColor = Color(0xFF00796B)
                    ),
                    supportingText = {
                        if (viewModel.emailError != null) {
                            Text(viewModel.emailError!!)
                        }
                    }
                )

                Spacer(Modifier.height(12.dp))



                // Passwort mit Sichtbarkeits-Toggle
                var showPw by remember { mutableStateOf(false) }
                OutlinedTextField(
                    value = viewModel.password,
                    onValueChange = viewModel::onPasswordChange,
                    label = { Text("Passwort") },
                    leadingIcon = { Icon(Icons.Filled.Lock, null) },
                    trailingIcon = {
                        IconButton(onClick = { showPw = !showPw }) {
                            Icon(
                                imageVector = if (showPw) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                contentDescription = if (showPw) "Passwort ausblenden" else "Passwort anzeigen"
                            )
                        }
                    },
                    singleLine = true,
                    visualTransformation = if (showPw) VisualTransformation.None else PasswordVisualTransformation(),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF00796B),
                        cursorColor = Color(0xFF00796B),
                        focusedLabelColor = Color(0xFF00796B)
                    )
                )

                Spacer(Modifier.height(20.dp))


                // Registrieren-Button
                Button(
                    onClick = {
                        focusManager.clearFocus()
                        viewModel.registerUser(context) {
                            Toast.makeText(context, "Willkommen!", Toast.LENGTH_LONG).show()
                            navController.navigate("home") {
                                popUpTo("register") { inclusive = true }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(R.color.button_normal),
                        contentColor = Color.White,
                        disabledContainerColor = colorResource(R.color.button_normal).copy(alpha = 0.5f)
                    )
                ) {
                    Text("REGISTRIEREN")
                }

                Spacer(Modifier.height(8.dp))


                // Status / Message
                if (viewModel.message.isNotBlank()) {
                    Text(
                        text = viewModel.message,
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Spacer(Modifier.height(8.dp))



                Row {
                    Text("Schon registriert?")
                    Text(
                        text = "Login",
                        color = colorResource(id = R.color.button_normal),
                        modifier = Modifier.clickable { navController.navigate("login") }
                    )
                }



            }



        }





    }

}