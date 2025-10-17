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
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.sprayconnectapp.R

@Composable
fun ResetPasswordScreen(
    navController: NavController,
    tokenArg: String?,
    vm: ResetPasswordViewModel = viewModel()
) {
    val ctx = LocalContext.current
    var showNewPw by remember { mutableStateOf(false) }
    var showConfirmPw by remember { mutableStateOf(false) }

    // Farben (kein System-Theme)
    val brand = colorResource(R.color.button_normal)     // #3F888F
    val borderUnfocused = Color(0xFFB0B0B0)
    val labelUnfocused = Color(0xFF606060)
    val placeholderCol = Color(0xFF7A7A7A)
    val iconUnfocused = Color(0xFF6A6A6A)
    val errorRed = Color(0xFFD32F2F)

    val screenBg = Brush.verticalGradient(
        colors = listOf(Color(0xFF2E2E2E), Color(0xFF4D4D4D))
    )
    val headerShape = RoundedCornerShape(bottomStart = 36.dp, bottomEnd = 36.dp)

    // === Tokenprüfung beim Start ===
    LaunchedEffect(tokenArg) {
        tokenArg?.takeIf { it.isNotBlank() }?.let { vm.applyToken(it) }
        vm.validateToken(ctx)
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(screenBg)
    ) {
        val headerRatio = 0.38f
        val headerHeight = maxHeight * headerRatio
        val cardOverlap = 100.dp

        // ===== HEADER =====
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

        // ===== CARD =====
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
            // TextField-Stil
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
                        "Neues Passwort setzen",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.Black
                    )

                    Spacer(Modifier.height(12.dp))

                    when (vm.tokenValid) {
                        null -> {
                            Text("Prüfe Link…", color = Color.Black)
                        }

                        false -> {
                            Text(
                                "Der Link ist ungültig oder abgelaufen.",
                                color = errorRed,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(Modifier.height(8.dp))
                            TextButton(
                                onClick = { navController.navigate("forgot") },
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("Neuen Link anfordern", color = brand)
                            }
                        }

                        true -> {
                            // Neues Passwort
                            OutlinedTextField(
                                value = vm.newPassword,
                                onValueChange = vm::onNewPasswordChange,
                                label = { Text("Neues Passwort") },
                                singleLine = true,
                                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                                trailingIcon = {
                                    IconButton(onClick = { showNewPw = !showNewPw }) {
                                        Icon(
                                            imageVector = if (showNewPw) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                            contentDescription = if (showNewPw) "Passwort verbergen" else "Passwort anzeigen"
                                        )
                                    }
                                },
                                visualTransformation = if (showNewPw) VisualTransformation.None else PasswordVisualTransformation(),
                                isError = vm.newPwError != null,
                                modifier = Modifier.fillMaxWidth(),
                                colors = tfColors, // deine bestehenden Farben
                                textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.Black),
                                shape = RoundedCornerShape(50),
                                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next)
                            )
                            if (vm.newPwError != null) {
                                Text(
                                    vm.newPwError!!,
                                    color = errorRed,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier
                                        .align(Alignment.Start)
                                        .padding(top = 4.dp)
                                )
                            }

                            Spacer(Modifier.height(12.dp))

// Passwort bestätigen
                            OutlinedTextField(
                                value = vm.confirm,
                                onValueChange = vm::onConfirmChange,
                                label = { Text("Passwort bestätigen") },
                                singleLine = true,
                                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                                trailingIcon = {
                                    IconButton(onClick = { showConfirmPw = !showConfirmPw }) {
                                        Icon(
                                            imageVector = if (showConfirmPw) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                            contentDescription = if (showConfirmPw) "Passwort verbergen" else "Passwort anzeigen"
                                        )
                                    }
                                },
                                visualTransformation = if (showConfirmPw) VisualTransformation.None else PasswordVisualTransformation(),
                                isError = vm.confirmError != null,
                                modifier = Modifier.fillMaxWidth(),
                                colors = tfColors,
                                textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.Black),
                                shape = RoundedCornerShape(50),
                                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(onDone = {
                                    vm.submit(ctx) { navController.navigate("login") { popUpTo(0) } }
                                })
                            )
                            if (vm.confirmError != null) {
                                Text(
                                    vm.confirmError!!,
                                    color = errorRed,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier
                                        .align(Alignment.Start)
                                        .padding(top = 4.dp)
                                )
                            }

                            Spacer(Modifier.height(16.dp))

                            // Button
                            Button(
                                onClick = {
                                    vm.submit(ctx) {
                                        navController.navigate("login") { popUpTo(0) }
                                    }
                                },
                                enabled = !vm.isLoading,
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
                                    Text("PASSWORT SPEICHERN")
                                }
                            }
                        }
                    }
                }
            }
        }

        // Footer
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
        }
    }
}
