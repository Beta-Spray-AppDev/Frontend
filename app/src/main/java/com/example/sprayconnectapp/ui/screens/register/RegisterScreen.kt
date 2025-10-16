package com.example.sprayconnectapp.ui.screens.register

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.sprayconnectapp.R


/**
 * Registrierungs-Screen:
 * - Username, E-Mail (mit Blur-Validierung), Passwort (mit Sichtbarkeits-Toggle)
 * - Button ist nur aktiv, wenn ViewModel-Validierung dies erlaubt
 * - Bei Erfolg: Auto-Login + Navigation nach Home
 */
@Composable
fun RegisterScreen(
    navController: NavController,
    viewModel: RegisterViewModel = viewModel()
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    // 🎨 Markenfarbe
    val brand = colorResource(R.color.button_normal)          // #3F888F
    val borderUnfocused = Color(0xFFB0B0B0)
    val labelUnfocused  = Color(0xFF606060)
    val placeholderCol  = Color(0xFF7A7A7A)
    val iconUnfocused   = Color(0xFF6A6A6A)
    val errorRed = Color(0xFFD32F2F)

    // Screen-Hintergrund (gleich wie Login)
    val screenBg = Brush.verticalGradient(
        colors = listOf(Color(0xFF2E2E2E), Color(0xFF4D4D4D))
    )

    // Header-Geometrie (gleich wie Login)
    val headerShape = RoundedCornerShape(bottomStart = 36.dp, bottomEnd = 36.dp)

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(screenBg)
    ) {
        val headerRatio = 0.38f
        val headerHeight = maxHeight * headerRatio
        val cardOverlap = 100.dp

        // ===== HEADER: weißer Background, Logo + sprayconnect mittig =====
        Surface(
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
            color = Color.Transparent,
            shape = headerShape,
            modifier = Modifier
                .fillMaxWidth()
                .height(headerHeight)
                .align(Alignment.TopCenter)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(headerShape),
                contentAlignment = Alignment.Center
            ) {
                // Weißer Hintergrund
                Box(Modifier.matchParentSize().background(Color.White))

                // Inhalt zentriert – mit Bottom-Padding, damit Card-Overlap nichts verdeckt
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = cardOverlap),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Dein Logo (gleich wie Login)
                    Image(
                        painter = painterResource(R.drawable.logokreis),
                        contentDescription = null,
                        modifier = Modifier
                            .size(160.dp)
                            .alpha(0.7f),
                        contentScale = ContentScale.Fit,
                        colorFilter = ColorFilter.tint(brand, blendMode = BlendMode.SrcAtop)
                    )

                    Spacer(Modifier.height(4.dp))

                    // sprayconnect (zweifarbig, gleich wie Login)
                    Text(
                        text = buildAnnotatedString {
                            withStyle(SpanStyle(color = brand, fontWeight = FontWeight.Light)) {
                                append("spray")
                            }
                            withStyle(SpanStyle(color = Color(0xFF1E4E52), fontWeight = FontWeight.Normal)) {
                                append("connect")
                            }
                        },
                        style = MaterialTheme.typography.headlineLarge,
                        textAlign = TextAlign.Center,
                        color = Color.Unspecified
                    )
                }
            }
        }

        // ===== CARD (ident zu Login) =====
        Card(
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .align(Alignment.TopCenter)
                .offset(y = headerHeight - cardOverlap),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFCFCFCF))
        ) {
            // Einheitliche TextField-Farben – ident zu Login
            val tfColors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = brand,
                unfocusedBorderColor = borderUnfocused,
                disabledBorderColor = borderUnfocused.copy(alpha = 0.6f),
                errorBorderColor = MaterialTheme.colorScheme.error,

                focusedLabelColor = brand,
                unfocusedLabelColor = labelUnfocused,
                disabledLabelColor = labelUnfocused.copy(alpha = 0.6f),

                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                disabledTextColor = Color.Black.copy(alpha = 0.6f),

                focusedPlaceholderColor = placeholderCol,
                unfocusedPlaceholderColor = placeholderCol,
                disabledPlaceholderColor = placeholderCol.copy(alpha = 0.6f),

                focusedLeadingIconColor = brand,
                unfocusedLeadingIconColor = iconUnfocused,
                focusedTrailingIconColor = brand,
                unfocusedTrailingIconColor = iconUnfocused,
                disabledLeadingIconColor = iconUnfocused.copy(alpha = 0.6f),
                disabledTrailingIconColor = iconUnfocused.copy(alpha = 0.6f),

                cursorColor = brand,

                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                disabledContainerColor = Color.White,
                errorContainerColor     = Color.White
            )

            // Falls deine Compose-Version selectionColors hier nicht kennt, nutze den Provider
            CompositionLocalProvider(
                LocalTextSelectionColors provides TextSelectionColors(
                    handleColor = brand,
                    backgroundColor = brand.copy(alpha = 0.25f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Registrieren",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.Black
                    )

                    Spacer(Modifier.height(16.dp))

                    // Benutzername
                    OutlinedTextField(
                        value = viewModel.username,
                        onValueChange = viewModel::onUsernameChange,
                        label = { Text("Benutzername") },
                        leadingIcon = { Icon(Icons.Filled.Person, null) },
                        singleLine = true,
                        isError = viewModel.usernameError != null,
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.Black),
                        colors = tfColors,
                        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions { focusManager.moveFocus(FocusDirection.Down) }
                    )

                    if (viewModel.usernameError != null) {
                        Text(
                            text = viewModel.usernameError ?: "",
                            color = errorRed,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier
                                .align(Alignment.Start)
                                .padding(top = 4.dp)
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    // E-Mail
                    OutlinedTextField(
                        value = viewModel.email,
                        onValueChange = viewModel::onEmailChange,
                        label = { Text("E-Mail") },
                        leadingIcon = { Icon(Icons.Filled.Email, null) },
                        singleLine = true,
                        isError = viewModel.emailError != null,
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.Black),
                        colors = tfColors,
                        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions { focusManager.moveFocus(FocusDirection.Down) }
                    )

                    if (viewModel.emailError != null) {
                        Text(
                            text = viewModel.emailError ?: "",
                            color = errorRed,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier
                                .align(Alignment.Start)
                                .padding(top = 4.dp)
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    // Passwort
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
                                    contentDescription = null
                                )
                            }
                        },
                        singleLine = true,
                        visualTransformation = if (showPw) VisualTransformation.None else PasswordVisualTransformation(),
                        isError = viewModel.passwordError != null,
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.Black),
                        colors = tfColors,
                        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                                viewModel.registerUser(context) {
                                    Toast.makeText(context, "Willkommen!", Toast.LENGTH_LONG).show()
                                    navController.navigate("home") {
                                        popUpTo("register") { inclusive = true }
                                    }
                                }
                            }
                        )
                    )

                    if (viewModel.passwordError != null) {
                        Text(
                            text = viewModel.passwordError ?: "",
                            color = errorRed,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .padding(top = 4.dp)
                        )
                    }

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
                        enabled = viewModel.canSubmit(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .wrapContentWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = brand,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("REGISTRIEREN")
                    }

                    Spacer(Modifier.height(8.dp))

                    // Link zum Login
                    Row {
                        Text("Schon registriert? ", color = Color.Black)
                        Text(
                            text = "Login",
                            color = brand,
                            modifier = Modifier.clickable { navController.navigate("login") }
                        )
                    }
                }
            }
        }

        // Footer (gleich wie Login)
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
