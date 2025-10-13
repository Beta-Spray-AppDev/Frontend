package com.example.sprayconnectapp.ui.screens.Profile

import android.util.Patterns
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
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
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

    fun isValidEmail(s: String): Boolean = Patterns.EMAIL_ADDRESS.matcher(s).matches()
    var emailError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) { viewModel.loadProfile(context) }

    // Prefill
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
    ) {
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
                    elevation = CardDefaults.cardElevation(6.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFE5E5E5), // Grau-Hintergrund für die Card
                        contentColor   = Color(0xFF000000)  // schwarzer Content
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .padding(24.dp)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            "Nutzerdaten bearbeiten",
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color(0xFF000000)
                        )
                        Divider(color = Color(0x1F000000)) // 12% Schwarz

                        // Einheitliche Farben für Textfelder (weiß + Akzent = button_normal, Text/Icon = schwarz)
                        val tfColors = OutlinedTextFieldDefaults.colors(
                            // Container (Hintergrund der Textfelder)
                            focusedContainerColor   = Color(0xFFFFFFFF),
                            unfocusedContainerColor = Color(0xFFFFFFFF),
                            disabledContainerColor  = Color(0xFFFFFFFF),

                            // Rahmen/Label/Cursor mit App-Akzent
                            focusedBorderColor = colorResource(R.color.button_normal),
                            unfocusedBorderColor = colorResource(R.color.button_normal),
                            disabledBorderColor = colorResource(R.color.button_normal),
                            focusedLabelColor = colorResource(R.color.button_normal),
                            cursorColor = colorResource(R.color.button_normal),

                            // Textfarben
                            focusedTextColor   = Color(0xFF000000),
                            unfocusedTextColor = Color(0xFF000000),
                            disabledTextColor  = Color(0xFF000000),

                            // Icon-Farben (Leading/Trailing)
                            focusedLeadingIconColor   = Color(0xFF000000),
                            unfocusedLeadingIconColor = Color(0xFF000000),
                            disabledLeadingIconColor  = Color(0xFF000000),

                            focusedTrailingIconColor   = Color(0xFF000000),
                            unfocusedTrailingIconColor = Color(0xFF000000),
                            disabledTrailingIconColor  = Color(0xFF000000),

                            // Placeholder/Supporting
                            focusedPlaceholderColor   = Color(0xFF000000),
                            unfocusedPlaceholderColor = Color(0xFF000000),
                            disabledPlaceholderColor  = Color(0xFF000000)
                        )

                        fun saveProfile() {
                            if (isLoading) return
                            if (email.isNotBlank() && !isValidEmail(email)) {
                                emailError = "Bitte gib eine gültige E-Mail-Adresse ein."
                                return
                            }
                            viewModel.updateProfile(
                                context = context,
                                username = username.trim(),
                                email = email.trim(),
                                password = password.takeIf { it.isNotBlank() } ?: "",
                                onSuccess = {
                                    password = ""
                                    navController.popBackStack()
                                    Toast.makeText(context, "Profil gespeichert", Toast.LENGTH_LONG).show()
                                },
                                onError = { errorMessage = it }
                            )
                        }

                        // Username
                        OutlinedTextField(
                            value = username,
                            onValueChange = { username = it },
                            label = { Text("Benutzername", color = Color(0xFF000000)) },
                            placeholder = { Text("Benutzername", color = Color(0xFF000000)) },
                            leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null) },
                            colors = tfColors,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(50),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Down) }
                            )
                        )

                        // Email
                        OutlinedTextField(
                            value = email,
                            onValueChange = {
                                email = it
                                emailError = if (it.isNotBlank() && !isValidEmail(it))
                                    "Bitte gib eine gültige E-Mail-Adresse ein."
                                else null
                            },
                            label = { Text("E-Mail", color = Color(0xFF000000)) },
                            placeholder = { Text("E-Mail", color = Color(0xFF000000)) },
                            leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null) },
                            colors = tfColors,
                            isError = emailError != null,
                            supportingText = {
                                if (emailError != null) {
                                    Text(emailError!!, color = Color(0xFFD32F2F))
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(50),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Down) }
                            )
                        )

                        // Passwort
                        OutlinedTextField(
                            shape = RoundedCornerShape(50),
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Neues Passwort", color = Color(0xFF000000)) },
                            placeholder = { Text("Neues Passwort", color = Color(0xFF000000)) },
                            leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
                            colors = tfColors,
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                val (icon, desc) =
                                    if (passwordVisible) Icons.Default.VisibilityOff to "Passwort verbergen"
                                    else Icons.Default.Visibility to "Passwort anzeigen"

                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(icon, contentDescription = desc, tint = Color(0xFF000000))
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

                        // Speichern
                        Button(
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colorResource(R.color.button_normal),
                                contentColor = Color(0xFFFFFFFF)
                            ),
                            onClick = { saveProfile() },
                            enabled = !isLoading,
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .wrapContentWidth()
                        ) { Text("Speichern") }

                        // Info/Fehler
                        infoMessage?.let {
                            Text(it, color = Color(0xFF000000))
                        }
                        errorMessage?.let {
                            Text(it, color = Color(0xFFD32F2F))
                        }

                        if (isLoading) {
                            CircularProgressIndicator(
                                color = colorResource(R.color.button_normal),
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                        }

                        error?.let {
                            Text("Fehler: $it", color = Color(0xFFD32F2F))
                        }
                    }
                }
            }
        }
    }
}
