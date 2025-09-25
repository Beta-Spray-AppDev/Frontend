package com.example.sprayconnectapp.ui.screens.Profile

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction
import com.example.sprayconnectapp.R


/**
 * Screen zum Bearbeiten von Profildaten.
 * - Lädt initial das Profil
 * - Prefill der Felder (Username/E-Mail)
 * - Optionales Passwort-Update (leer = nicht ändern)
 * - Speichern triggert Update im ViewModel
 */


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(navController: NavController) {
    val context = LocalContext.current
    val viewModel: ProfileViewModel = viewModel()

    //Viewmodel state
    val profile by viewModel.profile.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    // Eingabefelder
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }


    // UI-Meldungen
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var infoMessage by remember { mutableStateOf<String?>(null) }


    var passwordVisible by rememberSaveable { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current



    LaunchedEffect(Unit) {
        viewModel.loadProfile(context)
    }

    // Nur beim ersten Mal mit aktuellen Werten befüllen
    LaunchedEffect(profile) {
        profile?.let {
            username = it.username
            email = it.email ?: ""
        }
    }

    val BarColor = colorResource(id = R.color.hold_type_bar)



    //Farbverlauf
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
                    title = { Text("Profil bearbeiten") },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Zurück")
                        }
                    }
                )
            }
        ) { innerPadding ->


            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(16.dp)
                    .widthIn(max = 560.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

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
                        Text("Nutzerdaten bearbeiten", style = MaterialTheme.typography.headlineSmall)
                        Divider()

                        // Einheitliche Farben für Textfelder
                        val tfColors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            disabledContainerColor = Color.White,
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black
                        )


                        // lokaler Helper: verhindert Doppel-Klicks und ruft VM-Update
                        fun saveProfile (){
                            if (isLoading) return // doppelte Klicks vermeiden

                            viewModel.updateProfile(
                                context = context,
                                username = username.trim(),
                                email = email.trim(),
                                // leeres Passwort bedeutet "nicht ändern"
                                password = password.takeIf { it.isNotBlank() } ?: "",
                                onSuccess = {
                                    password = ""
                                    navController.popBackStack()
                                    Toast.makeText(context, "Profil gespeichert", Toast.LENGTH_LONG).show()
                                },
                                onError = { errorMessage = it }
                            )
                        }


                        // Username Eingabe
                        OutlinedTextField(
                            value = username,
                            onValueChange = { username = it },
                            label = { Text("Benutzername") },
                            colors = tfColors,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Down) }
                            )
                        )

                        // Email Eingabe
                        OutlinedTextField(
                            value = email,
                            colors = tfColors,
                            onValueChange = { email = it },
                            label = { Text("E-Mail") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Down) }
                            )
                        )


                        // Passwort Eingabe
                        OutlinedTextField(
                            shape = RoundedCornerShape(12.dp),
                            value = password,
                            colors = tfColors,
                            onValueChange = { password = it },
                            label = { Text("Neues Passwort") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            visualTransformation = if (passwordVisible)
                                VisualTransformation.None
                            else PasswordVisualTransformation(),
                            trailingIcon = {
                                val (icon, desc) =
                                    if (passwordVisible) {
                                        Icons.Default.VisibilityOff to "Passwort verbergen"
                                    } else {
                                        Icons.Default.Visibility to "Passwort anzeigen"
                                    }

                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(icon, contentDescription = desc)
                                }
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    focusManager.clearFocus()
                                    saveProfile()
                                }
                            )
                        )

                        // Speichern-Button
                        Button(
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colorResource(R.color.button_normal),
                                contentColor = Color.White
                            ),
                            onClick = {
                                saveProfile()
                            },
                            enabled = !isLoading,
                            modifier = Modifier
                                .wrapContentWidth()
                        ) {
                            Text("Speichern")
                        }


                        // wie Profil aktualisiert
                        infoMessage?.let {
                            Text(it, color = MaterialTheme.colorScheme.primary)
                        }

                        // Fehlertext
                        errorMessage?.let {
                            Text(it, color = MaterialTheme.colorScheme.error)
                        }

                        if (isLoading) {
                            CircularProgressIndicator(color = colorResource(R.color.button_normal))
                        }

                        error?.let {
                            Text("Fehler: $it", color = MaterialTheme.colorScheme.error)
                        }

                    }

                }

            }



        }




    }



}
