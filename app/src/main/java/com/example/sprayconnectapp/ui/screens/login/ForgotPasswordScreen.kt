package com.example.sprayconnectapp.ui.screens.login

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.sprayconnectapp.R

@Composable
fun ForgotPasswordScreen(
    navController: NavController,
    vm: ForgotPasswordViewModel = viewModel()
) {
    val ctx = LocalContext.current
    val canSubmit = vm.email.isNotBlank() && !vm.isLoading

    // === Marken- & UI-Farben (kein Systemschema) ===
    val brand            = colorResource(R.color.button_normal)     // #3F888F
    val borderUnfocused  = Color(0xFFB0B0B0)
    val labelUnfocused   = Color(0xFF606060)
    val placeholderCol   = Color(0xFF7A7A7A)
    val iconUnfocused    = Color(0xFF6A6A6A)
    val errorRed         = Color(0xFFD32F2F)

    // Screen-Hintergrund (wie Login/Registrieren)
    val screenBg = Brush.verticalGradient(
        colors = listOf(Color(0xFF2E2E2E), Color(0xFF4D4D4D))
    )
    val headerShape = RoundedCornerShape(bottomStart = 36.dp, bottomEnd = 36.dp)

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(screenBg)
    ) {
        val headerRatio  = 0.38f
        val headerHeight = maxHeight * headerRatio
        val cardOverlap  = 100.dp

        // ===== HEADER: Weiß, Logo + sprayconnect zentriert =====
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
                Box(Modifier.matchParentSize().background(Color.White))
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = cardOverlap),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Image(
                        painter = painterResource(R.drawable.logokreis),
                        contentDescription = null,
                        modifier = Modifier
                            .size(160.dp)
                            .alpha(0.7f),
                        colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(
                            brand, blendMode = BlendMode.SrcAtop
                        )
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = buildAnnotatedString {
                            withStyle(SpanStyle(color = Color(0xFF3F888F), fontWeight = FontWeight.Light)) {
                                append("spray")
                            }
                            withStyle(SpanStyle(color = Color(0xFF1E4E52), fontWeight = FontWeight.Normal)) {
                                append("connect")
                            }
                        },
                        style = MaterialTheme.typography.headlineLarge,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // ===== CARD (schwebend) =====
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
            // Einheitliche TextField-Farben (wie Login/Registrieren)
            val tfColors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = brand,
                unfocusedBorderColor = borderUnfocused,
                disabledBorderColor = borderUnfocused.copy(alpha = 0.6f),
                errorBorderColor = errorRed,

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

                cursorColor = brand,

                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                disabledContainerColor = Color.White,
                errorContainerColor = Color.White

            )

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
                        "Passwort vergessen",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.Black
                    )

                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Gib deine E-Mail ein. Wenn sie bei uns existiert, senden wir dir einen Link zum Zurücksetzen.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Black
                    )

                    Spacer(Modifier.height(16.dp))

                    OutlinedTextField(
                        value = vm.email,
                        onValueChange = vm::onEmailChange,
                        label = { Text("E-Mail") },
                        singleLine = true,
                        leadingIcon = { Icon(imageVector = Icons.Default.Email, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        isError = vm.emailError != null,
                        colors = tfColors,
                        textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.Black),
                        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { vm.submit(ctx) }),
                        shape = RoundedCornerShape(50)
                    )

                    if (vm.emailError != null) {
                        Text(
                            vm.emailError!!,
                            color = errorRed,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier
                                .align(Alignment.Start)
                                .padding(top = 4.dp)
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    Button(
                        onClick = { vm.submit(ctx) },
                        enabled = canSubmit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = brand,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        if (vm.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("LINK SENDEN")
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    TextButton(
                        onClick = { navController.popBackStack() },
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            "Zurück zum Login",
                            style = MaterialTheme.typography.bodyMedium,
                            color = brand
                        )
                    }
                }
            }
        }

        // Footer (konsistent)
        Text(
            text = "Powered by MaltaCloud",
            color = Color.White.copy(alpha = 0.6f),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 12.dp)
        )
    }

    if (vm.message.isNotBlank()) {
        LaunchedEffect(vm.message) {
            Toast.makeText(ctx, vm.message, Toast.LENGTH_LONG).show()
            // Falls dein ViewModel eine clearMessage() hat, hier aufrufen.
            // vm.clearMessage()
        }
    }
}
