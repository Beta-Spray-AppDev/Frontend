package com.example.sprayconnectapp.ui.screens.home

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.sprayconnectapp.data.dto.CreateGymDTO
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Divider
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.Alignment
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.input.ImeAction
import com.example.sprayconnectapp.ui.screens.BottomNavigationBar
import com.example.sprayconnectapp.util.TokenStore
import com.example.sprayconnectapp.R


import java.lang.reflect.Modifier.isPublic
import java.util.UUID

/**
 * Formular zum Erstellen eines neuen Gyms.
 * - Validiert Basiseingaben
 * - Fügt createdBy aus dem Token hinzu
 */




@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddGymScreen(
    navController: NavController,
    viewModel: HomeViewModel = viewModel()
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    var name by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var isPublic by remember { mutableStateOf(true) }


    // UI State
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var infoMessage by remember { mutableStateOf<String?>(null) }

    // Farben & Styles (wie EditProfile)
    val barColor = colorResource(id = R.color.hold_type_bar)
    val buttonColor = colorResource(id = R.color.button_normal)

    val tfColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = Color(0xFF00796B),
        cursorColor = Color(0xFF00796B),
        focusedLabelColor = Color(0xFF00796B),
        unfocusedContainerColor = Color.White,
        focusedContainerColor = Color.White
    )
    val switchColors = SwitchDefaults.colors(
        checkedThumbColor = Color.White,
        checkedTrackColor = buttonColor,
        uncheckedThumbColor = Color.White,
        uncheckedTrackColor = Color(0xFFE0E0E0)
    )
    val screenBg = Brush.verticalGradient(
        colors = listOf(Color(0xFF53535B), Color(0xFF767981), Color(0xFFA8ABB2))
    )

    fun canSave() = !isLoading && name.isNotBlank() && location.isNotBlank()

    fun saveGym() {
        if (!canSave()) return
        isLoading = true
        errorMessage = null
        infoMessage = null


        val store = TokenStore.create(context)
        val userId = store.getUserId()

        if (userId != null) {
            val dto = CreateGymDTO(
                name = name.trim(),
                location = location.trim(),
                description = description.trim(),
                createdBy = UUID.fromString(userId),
                isPublic = isPublic
            )
            viewModel.createGym(
                context = context,
                dto = dto,
                onSuccess = {
                    isLoading = false
                    Toast.makeText(context, "Gym erfolgreich erstellt!", Toast.LENGTH_LONG).show()
                    navController.popBackStack()
                },
                onError = {
                    isLoading = false
                    errorMessage = it
                }
            )
        } else {
            isLoading = false
            errorMessage = "Fehler: Benutzer nicht erkannt"
        }
    }


    Box( modifier = Modifier
        .fillMaxSize()
        .background(screenBg)){


        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = barColor,
                        scrolledContainerColor = barColor,
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White,
                        actionIconContentColor = Color.White
                    ),
                    title = { Text("Neues Gym hinzufügen") },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Zurück")
                        }
                    }
                )
            },
            bottomBar = { BottomNavigationBar(navController) }
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp)
                    .widthIn(max = 560.dp)
                    .fillMaxSize()
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { focusManager.clearFocus() },
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
                        Text(
                            "Gym anlegen",
                            style = MaterialTheme.typography.headlineSmall,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                        Divider()

                        OutlinedTextField(
                            value = name,
                            onValueChange = { if (it.length <= 20) name = it },
                            label = { Text("Name") },
                            placeholder = { Text("Name des Gyms") },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                            colors = tfColors,
                            shape = RoundedCornerShape(50),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Down) }
                            )
                        )

                        OutlinedTextField(
                            value = location,
                            onValueChange = { if (it.length <= 20) location = it },
                            label = { Text("Ort") },
                            placeholder = { Text("Stadt/Adresse") },
                            leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                            colors = tfColors,
                            shape = RoundedCornerShape(50),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Down) }
                            )
                        )

                        OutlinedTextField(
                            value = description,
                            onValueChange = { if (it.length <= 100) description = it },
                            label = { Text("Beschreibung") },
                            placeholder = { Text("Kurzbeschreibung") },
                            leadingIcon = { Icon(Icons.Default.Info, contentDescription = null) },
                            colors = tfColors,
                            shape = RoundedCornerShape(50),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    focusManager.clearFocus()
                                    saveGym()
                                }
                            )
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Switch(
                                checked = isPublic,
                                onCheckedChange = { isPublic = it },
                                colors = switchColors
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Öffentlich sichtbar")
                        }

                        Button(
                            onClick = { saveGym() },
                            enabled = canSave(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = buttonColor,
                                contentColor = Color.White
                            ),
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .wrapContentWidth()
                        ) {
                            Text("Speichern")
                        }

                        infoMessage?.let { Text(it, color = MaterialTheme.colorScheme.primary) }
                        errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }

                        if (isLoading) {
                            CircularProgressIndicator(
                                color = buttonColor,
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                        }
                    }
                }
            }
        }
    }



}


