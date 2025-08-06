package com.example.sprayconnectapp.ui.screens.register

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

@Composable
fun RegisterScreen(navController: NavController, viewModel: RegisterViewModel = viewModel()) {




    Box(modifier = Modifier
        .fillMaxSize()
        .padding(top = 16.dp)){




        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
        ) {


            IconButton(
                onClick = {
                    Log.d("RegisterScreen", "Zurück geklickt!")

                    navController.popBackStack()},
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(16.dp)
                    .zIndex(1f)
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Zurück")
            }

            Spacer(modifier = Modifier.height(16.dp))
            val context = LocalContext.current


            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 16.dp)
            ){
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ){
                    Text("Registrieren", style = MaterialTheme.typography.headlineLarge)

                    Spacer(modifier = Modifier.height(24.dp))

                    OutlinedTextField(
                        value = viewModel.username,
                        onValueChange = viewModel::onUsernameChange,
                        label = { Text("Benutzername") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))


                    OutlinedTextField(
                        value = viewModel.email,
                        onValueChange = viewModel::onEmailChange,
                        label = { Text("E-Mail") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = viewModel.password,
                        onValueChange = viewModel::onPasswordChange,
                        label = { Text("Passwort") },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = PasswordVisualTransformation()
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = {
                            viewModel.registerUser(context) {
                                Toast.makeText(context, "Willkommen!", Toast.LENGTH_LONG).show()
                                navController.navigate("home") {
                                    popUpTo("register") { inclusive = true }
                                }
                            }
                                  },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Registrieren")
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = viewModel.message,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }


        }


    }

}