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
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
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


    val headerShape = RoundedCornerShape(bottomStart = 36.dp, bottomEnd = 36.dp)

    var showPw by remember { mutableStateOf(false) }

    var hadEmailFocus by remember { mutableStateOf(false) }










    Box(modifier = Modifier
        .fillMaxSize()
        .background(screenBg)
    ){

        // Header
        Surface(
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
            color = Color.Transparent,
            shape = RoundedCornerShape(bottomStart = 36.dp, bottomEnd = 36.dp),
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
                    .padding(top = 48.dp) // Abstand von oben
            ) {


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

        }

        // Card mit Formular
        Card(
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE0E0E0)), // helles Grau
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
                        focusedLabelColor = Color(0xFF00796B),
                        unfocusedContainerColor = Color.White,
                        focusedContainerColor = Color.White    
                    ),
                    supportingText = {
                        // Fehlermeldung falls vorhanden
                        if (viewModel.usernameError != null) {
                            Text(viewModel.usernameError!!)
                        }
                    },
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    )

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
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { f ->
                            if (f.isFocused) {
                                hadEmailFocus = true
                            } else if (hadEmailFocus && viewModel.email.isNotBlank()) {
                                viewModel.onEmailBlur()
                            } // zeigt Fehler nach Verlassen an
                        }
                    ,
                    isError = viewModel.emailError != null,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF00796B),
                        cursorColor = Color(0xFF00796B),
                        focusedLabelColor = Color(0xFF00796B),
                        unfocusedContainerColor = Color.White,
                        focusedContainerColor = Color.White
                    ),
                    supportingText = {
                        if (viewModel.emailError != null) {
                            Text(viewModel.emailError!!)
                        }
                    },
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    )
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
                        focusedLabelColor = Color(0xFF00796B),
                        unfocusedContainerColor = Color.White,
                        focusedContainerColor = Color.White
                    ),
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                            viewModel.registerUser(context) {
                                Toast.makeText(context, "Willkommen!", Toast.LENGTH_LONG).show()
                                navController.navigate("home") {
                                    popUpTo("register") { inclusive = true }
                                }
                            }                        }
                    )
                )

                Spacer(Modifier.height(20.dp))


                val canSubmit = viewModel.canSubmit()


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
                    enabled = canSubmit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .wrapContentWidth(),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(R.color.button_normal),
                        contentColor = Color.White
                    )
                ) {
                    Text("REGISTRIEREN")
                }

                Spacer(Modifier.height(8.dp))


                // Status / Message
                if (viewModel.message.isNotBlank()) {
                    Text(
                        text = viewModel.message,
                        color = colorResource(id = R.color.button_normal),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Spacer(Modifier.height(8.dp))



                Row {
                    Text("Schon registriert? ")
                    Text(
                        text = "Login",
                        color = colorResource(id = R.color.button_normal),
                        modifier = Modifier.clickable { navController.navigate("login") }
                    )
                }



            }



        }

        Text(
            text = "Powered by MaltaCloud",
            color = Color.White.copy(alpha = 0.6f),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 12.dp)
        )





    }

}