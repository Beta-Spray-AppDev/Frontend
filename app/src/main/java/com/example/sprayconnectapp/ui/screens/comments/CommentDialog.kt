package com.example.sprayconnectapp.ui.screens.comments

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.*
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.sprayconnectapp.R

@Composable
fun CommentDialog(
    onDismiss: () -> Unit,
    onSubmit: (text: String) -> Unit,
    maxLength: Int = 500
) {
    var text by remember { mutableStateOf("") }
    val remaining = maxLength - text.length
    val valid = text.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFFE5E5E5), // hellgrauer Hintergrund
        textContentColor = Color(0xFF000000), // schwarzer Text überall
        title = { Text("Kommentar schreiben", color = Color(0xFF000000)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = text,
                    onValueChange = { if (it.length <= maxLength) text = it },
                    label = { Text("Kommentar", color = Color(0xFF000000)) },
                    minLines = 3,
                    maxLines = 6,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colorResource(R.color.button_normal),
                        unfocusedBorderColor = colorResource(R.color.button_normal),
                        focusedLabelColor = colorResource(R.color.button_normal),
                        cursorColor = colorResource(R.color.button_normal),
                        focusedTextColor = Color(0xFF000000),
                        unfocusedTextColor = Color(0xFF000000)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    "Verbleibend: $remaining",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF000000)
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = valid,
                onClick = { if (valid) onSubmit(text.trim()) },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = colorResource(R.color.button_normal),
                    disabledContentColor = Color.LightGray
                )
            ) {
                Text("Senden")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Abbrechen", color = Color(0xFF000000)) // Schwarz
            }
        }
    )
}

