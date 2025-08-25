package com.example.sprayconnectapp.ui.screens.login

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
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
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.sprayconnectapp.R

import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import com.example.sprayconnectapp.ui.screens.isOnline


/**
 * Login-Screen:
 * - UI für Username/Passwort mit Validierung
 * - Triggert Login im ViewModel
 * - Navigiert nach erfolgreichem Login zur Home-Route
 * - Offline-Hinweis + Möglichkeit „Offline fortfahren“
 */

@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: LoginViewModel = viewModel()
) {
    val context = LocalContext.current
    val online = isOnline(context)

    val focusManager = LocalFocusManager.current // für Tastatur Enter


    val headerHeight = 240.dp
    val cardOverlap  = 40.dp


    // Button soll nur aktiv sein wenn Eingaben nicht leer sind und gerade kein Login läuft
    val canSubmit = viewModel.username.isNotBlank() && viewModel.password.isNotBlank() && !viewModel.isLoading



    val headerShape = RoundedCornerShape(bottomStart = 36.dp, bottomEnd = 36.dp)



    // Home wenn login erfolgreich
    if (viewModel.message == "Login erfolgreich") {
        LaunchedEffect(viewModel.message) { // Nav-Aufruf reagiert auf message
            navController.navigate("home") {
                popUpTo("login") { inclusive = true } // Backstack aufräumen damit man nicht mit zurück-Button dorthin kommt
            }
        }
    }

    // Hintergrundverlauf
    val screenBg = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF2E2E2E),
            Color(0xFF4D4D4D)
        )
    )


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(screenBg)
    ) {

        // oberer header gebogen
        Surface(
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
            color = Color.Transparent,
            shape = headerShape,
            modifier = Modifier
                .fillMaxWidth()
                .height(380.dp)
                .align(Alignment.TopCenter)
        ) {

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(headerShape)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF7FBABF),
                                Color(0xFF3F888F),
                                Color(0xFF2B5E63)
                            )
                        )
                    )
            ){
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 48.dp) //Abstand nach unten
            ) {

                // Logo
                Image(
                    painter = painterResource(R.drawable.start),
                    contentDescription = null,
                    modifier = Modifier
                        .size(90.dp)
                )
                Spacer(Modifier.height(8.dp))

                // Titel
                Text(
                    text = "SprayConnect",
                    color = Color.Black,
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center
                )
            }

        }
        }


        // Card mit Formularfeldern
        Card(
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .align(Alignment.TopCenter)
                .offset(y  = headerHeight - cardOverlap), // Card schwebt in Header hinein
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFE0E0E0)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Login",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.Black
                )

                Spacer(Modifier.height(16.dp))

                // Username-Feld + Fehlermeldung
                OutlinedTextField(
                    value = viewModel.username,
                    onValueChange = viewModel::onUsernameChange,
                    label = { Text("Benutzername") },
                    leadingIcon = { Icon(Icons.Filled.Person, null) },
                    singleLine = true,
                    isError = viewModel.usernameError != null, // Fehlerzustand steuert rote Darstellung
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF00796B),
                        cursorColor = Color(0xFF00796B),
                        focusedLabelColor = Color(0xFF00796B),
                        unfocusedContainerColor = Color.White,
                        focusedContainerColor = Color.White
                    ),
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    )

                )

                // // Username-Fehlermeldung
                if (viewModel.usernameError != null) {
                    Text(
                        text = viewModel.usernameError ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier
                            .align(Alignment.Start)
                            .padding(top = 4.dp)
                    )
                }

                Spacer(Modifier.height(12.dp))

                // Passwort mit show und hide
                var showPw by remember { mutableStateOf(false) }
                OutlinedTextField(
                    value = viewModel.password,
                    shape = RoundedCornerShape(50),
                    onValueChange = viewModel::onPasswordChange,
                    label = { Text("Passwort") },
                    leadingIcon = { Icon(Icons.Filled.Lock, null) },
                    trailingIcon = {
                        IconButton(onClick = { showPw = !showPw }) {
                            Icon(
                                imageVector = if (showPw) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                contentDescription = null
                            )
                        }
                    },
                    singleLine = true,
                    visualTransformation = if (showPw) VisualTransformation.None else PasswordVisualTransformation(),
                    isError = viewModel.passwordError != null,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF00796B),
                        cursorColor = Color(0xFF00796B),
                        focusedLabelColor = Color(0xFF00796B),
                        unfocusedContainerColor = Color.White,
                        focusedContainerColor = Color.White
                    ),
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        // Enter/Done löst sofort Login aus
                        onDone = {
                            focusManager.clearFocus()
                            viewModel.loginUser(context)
                        }
                    )

                    )

                // Passwort-Fehlermeldung
                if (viewModel.passwordError != null) {
                    Text(
                        text = viewModel.passwordError ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(top = 4.dp)
                    )
                }

                Spacer(Modifier.height(20.dp))

                // Login Button (zeigt Spinner, wenn Request läuft)
                Button(
                    onClick = { viewModel.loginUser(context) },
                    enabled = canSubmit && online,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .wrapContentWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(R.color.button_normal),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    if (viewModel.isLoading) {
                        //Spinner während des Requests
                        androidx.compose.material3.CircularProgressIndicator(
                            modifier = Modifier.size(20.dp), color = colorResource(R.color.button_normal)
                        )
                    } else {
                        Text("LOGIN")
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Status / Message
                if (viewModel.message.isNotBlank()) {
                    LaunchedEffect(viewModel.message) {
                        Toast.makeText(context, viewModel.message, Toast.LENGTH_LONG).show()
                        viewModel.clearMessage()
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Noch nicht registriert Button
                Row {
                    Text("Noch nicht registriert? ")
                    Text(
                        text = "Sign Up",
                        color = colorResource(id = R.color.button_normal),
                        modifier = Modifier.clickable(
                            enabled = online, // deaktiviert, wenn offline
                            onClick = { navController.navigate("register") }
                        )

                    )
                }

                // Offline-Hinweis + „Offline fortfahren“
                if (!online) {
                    Text(
                        text = "Du bist offline. Login & Registrierung nicht möglich.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            navController.navigate("home")
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Offline fortfahren")
                    }
                }
            }
        }

        // Kleiner Footer
        Text(
            text = "Powered by MaltaCloud",
            color = Color.White.copy(alpha = 0.6f),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 12.dp)
        )

    }

    fun isOnline(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val capabilities = cm.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}
