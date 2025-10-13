package com.example.sprayconnectapp.ui.screens.login

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.widget.Toast
import androidx.compose.animation.Animatable
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
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
import com.example.sprayconnectapp.ui.screens.isOnline
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector4D
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.TwoWayConverter
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.Image
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.runtime.*
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp


@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: LoginViewModel = viewModel()
) {
    val context = LocalContext.current
    val online = isOnline(context)
    val focusManager = LocalFocusManager.current

    val canSubmit = viewModel.username.isNotBlank() &&
            viewModel.password.isNotBlank() &&
            !viewModel.isLoading

    val headerShape = RoundedCornerShape(bottomStart = 36.dp, bottomEnd = 36.dp)

    // Bei Erfolg nach Home
    if (viewModel.message == "Login erfolgreich") {
        LaunchedEffect(viewModel.message) {
            navController.navigate("home") {
                popUpTo("login") { inclusive = true }
            }
        }
    }

    // Screen-Hintergrund (unten dunkel für Kontrast zur Card)
    val screenBg = Brush.verticalGradient(
        colors = listOf(Color(0xFF2E2E2E), Color(0xFF4D4D4D))
    )

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(screenBg)
    ) {
        // 📐 Responsive Maße
        val headerRatio = 0.38f
        val headerHeight = maxHeight * headerRatio
        val cardOverlap = 100.dp                 // konstanter Overlap

        // HEADER (Verlauf + Pattern-Bild über die ganze Breite)
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
                // Hintergrund weiß
                Box(Modifier.matchParentSize().background(Color.White))

                // Logo oben, Text darunter – beides mittig
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = cardOverlap),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    AnimatedMultiColorLogoWaveIntro()




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

        // CARD
        Card(
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .align(Alignment.TopCenter)
                .offset(y = headerHeight - cardOverlap), // schwebt leicht in den Header
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFCFCFCF)
            )
        ) {
            // ==== Farb-Setup ====
            val brand = colorResource(R.color.button_normal)          // #3F888F
            val borderUnfocused = Color(0xFFB0B0B0)                   // Rahmen grau (unfokussiert)
            val labelUnfocused  = Color(0xFF606060)
            val placeholderCol  = Color(0xFF7A7A7A)
            val iconUnfocused   = Color(0xFF6A6A6A)

            // Einheitliche TextField-Farben
            val tfColors = OutlinedTextFieldDefaults.colors(
                // Border
                focusedBorderColor = brand,
                unfocusedBorderColor = borderUnfocused,
                disabledBorderColor = borderUnfocused.copy(alpha = 0.6f),
                errorBorderColor = MaterialTheme.colorScheme.error,

                // Label
                focusedLabelColor = brand,
                unfocusedLabelColor = labelUnfocused,
                disabledLabelColor = labelUnfocused.copy(alpha = 0.6f),

                // Text (wenn deine Compose-Version die Props hat)
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                disabledTextColor = Color.Black.copy(alpha = 0.6f),

                // Placeholder
                focusedPlaceholderColor = placeholderCol,
                unfocusedPlaceholderColor = placeholderCol,
                disabledPlaceholderColor = placeholderCol.copy(alpha = 0.6f),

                // Icons
                focusedLeadingIconColor = brand,
                unfocusedLeadingIconColor = iconUnfocused,
                focusedTrailingIconColor = brand,
                unfocusedTrailingIconColor = iconUnfocused,
                disabledLeadingIconColor = iconUnfocused.copy(alpha = 0.6f),
                disabledTrailingIconColor = iconUnfocused.copy(alpha = 0.6f),

                // Cursor
                cursorColor = brand,

                // Container
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                disabledContainerColor = Color.White
            )

            // Falls deine Compose-Version KEINE selectionColors im colors()-Block kennt:
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
                        "Login",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.Black
                    )

                    Spacer(Modifier.height(16.dp))

                    // Username
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
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        )
                    )

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
                                viewModel.loginUser(context)
                            }
                        )
                    )

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

                    // Login-Button
                    Button(
                        onClick = { viewModel.loginUser(context) },
                        enabled = canSubmit && online,
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
                        if (viewModel.isLoading) {
                            androidx.compose.material3.CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("LOGIN")
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    // Status-Messages via Toast
                    if (viewModel.message.isNotBlank()) {
                        LaunchedEffect(viewModel.message) {
                            Toast.makeText(context, viewModel.message, Toast.LENGTH_LONG).show()
                            viewModel.clearMessage()
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    // Sign Up-Link
                    Row {
                        Text("Noch nicht registriert? ", color = Color.Black)
                        Text(
                            text = "Sign Up",
                            color = brand,
                            modifier = Modifier.clickable(
                                enabled = online,
                                onClick = { navController.navigate("register") }
                            )
                        )
                    }

                    // Offline-Hinweis
                    if (!online) {
                        Text(
                            text = "Du bist offline. Login & Registrierung nicht möglich.",
                            color = Color.Red,
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { navController.navigate("home") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = brand,
                                contentColor = Color.White
                            )
                        ) { Text("Offline fortfahren") }
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
}

// Helper
fun isOnline(context: Context): Boolean {
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = cm.activeNetwork ?: return false
    val capabilities = cm.getNetworkCapabilities(network) ?: return false
    return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
}


private val ColorVectorConverter: TwoWayConverter<Color, AnimationVector4D> =
    TwoWayConverter(
        convertToVector = { c -> AnimationVector4D(c.red, c.green, c.blue, c.alpha) },
        convertFromVector = { v -> Color(v.v1, v.v2, v.v3, v.v4) }
    )


@Composable
fun AnimatedMultiColorLogoWaveIntro(
    sizeDp: Dp = 160.dp,
    baseColor: Color = Color(0xFF3F888F)
) {
    val colors = listOf( //sunset
        Color(0xFF0E6E73), // petrol
        Color(0xFF00B8D9), // bright cyan
        Color(0xFF22D3EE), // aqua
        Color(0xFFFF8A65), // coral
        Color(0xFFFFCD38), // amber
        Color(0xFF3B82F6)  // bright blue
    )

    // „Boop“
    val scale = remember { Animatable(0.90f) }
    val alpha = remember { Animatable(0f) }

    // Wellenfortschritt 0f → 1f, plus weiches Ausblenden am Ende
    val progress = remember { Animatable(0f) }
    val gradientAlpha = remember { Animatable(1f) }

    LaunchedEffect(Unit) {
        // Boop + Fade-In
        scale.animateTo(1f, keyframes {
            durationMillis = 700
            1.10f at 260 with FastOutSlowInEasing
            0.98f at 520
            1.00f at 700
        })
    }
    LaunchedEffect(Unit) { alpha.animateTo(0.9f, tween(450)) }

    LaunchedEffect(Unit) {
        // Welle sweeped einmal rüber …
        progress.animateTo(1f, tween(durationMillis = 2200, easing = LinearEasing))
        // … und blendet dann weich aus (kein harter Cut)
        gradientAlpha.animateTo(0f, tween(durationMillis = 450))
    }

    Image(
        painter = painterResource(R.drawable.logokreis),
        contentDescription = null,
        modifier = Modifier
            .size(sizeDp)
            .graphicsLayer(scaleX = scale.value, scaleY = scale.value)
            .alpha(alpha.value)
            .drawWithCache {
                // Basis zuerst zeichnen (ruhige Markenfarbe)
                val baseBrush = SolidColor(baseColor)

                // Wave-Window (glockenförmig) – baut die Welle weich auf/ab
                val window = run {
                    val p = progress.value.coerceIn(0f, 1f)
                    // 0→1→0 Kurve (Dreieck), dann glätten
                    val tri = 1f - kotlin.math.abs(p * 2f - 1f)
                    // smoothstep
                    tri * tri * (3f - 2f * tri)
                }

                // Sweep-Geometrie: startet links außerhalb, endet rechts außerhalb
                val totalWidth = size.width
                val startX = -totalWidth + progress.value * (totalWidth * 2f)
                val endX = startX + totalWidth * 0.9f // Breite der „Welle“

                val gradBrush = Brush.linearGradient(
                    colors = colors,
                    start = Offset(startX, -totalWidth * 0.2f),
                    end = Offset(endX, totalWidth * 1.2f)
                )

                onDrawWithContent {
                    drawContent()
                    // Basisfarbe (falls dein Asset grau ist, lass das drin;
                    // wenn es schon farbig ist, kannst du diesen drawRect weglassen)
                    drawRect(baseBrush, blendMode = BlendMode.SrcAtop)

                    // Welle: Alpha wird von Fensterfunktion UND Ausblend-Alpha gesteuert
                    val a = (window * gradientAlpha.value).coerceIn(0f, 1f)
                    if (a > 0f) {
                        drawRect(
                            brush = gradBrush,
                            blendMode = BlendMode.SrcAtop,
                            alpha = a
                        )
                    }
                }
            },
        contentScale = ContentScale.Fit
    )
}






