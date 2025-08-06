package com.example.sprayconnectapp.ui.screens.spraywall

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.sprayconnectapp.data.dto.SpraywallDTO
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.*
import androidx.compose.ui.text.input.ImeAction



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSpraywallScreen(
    navController: NavController,
    gymId: String,
    gymName: String,
    viewModel: SpraywallViewModel = viewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var uploadedImageUrl by remember { mutableStateOf<String?>(null) }

    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var isPublic by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri -> imageUri = uri }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Neue Spraywall – $gymName") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Zurück")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) {
                    focusManager.clearFocus()
                }
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            imageUri?.let {
                AsyncImage(
                    model = it,
                    contentDescription = "Gewähltes Bild",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            Button(onClick = {
                launcher.launch("image/*")
            }) {
                Text("Bild auswählen")
            }

            OutlinedTextField(
                value = name,
                onValueChange = { if (it.length <= 20) name = it },
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                )
            )

            OutlinedTextField(
                value = description,
                onValueChange = { if (it.length <= 20) description = it },
                label = { Text("Beschreibung") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = { focusManager.clearFocus() }
                )
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(checked = isPublic, onCheckedChange = { isPublic = it })
                Spacer(modifier = Modifier.width(8.dp))
                Text("Öffentlich sichtbar")
            }

            Button(
                onClick = {
                    imageUri?.let { uri ->
                        uploadToNextcloudViaWebDAV(
                            context = context,
                            uri = uri,
                            onSuccess = { uploadedUrl ->
                                uploadedImageUrl = uploadedUrl
                                Log.d("UPLOAD", "Erfolgreich: $uploadedUrl")

                                val spraywallDto = SpraywallDTO(
                                    name = name,
                                    description = description,
                                    photoUrl = uploadedUrl,
                                    isPublic = isPublic,
                                    gymId = UUID.fromString(gymId)
                                )

                                viewModel.createSpraywall(
                                    context = context,
                                    dto = spraywallDto,
                                    onSuccess = {
                                        Toast.makeText(
                                            context,
                                            "Spraywall erfolgreich erstellt!",
                                            Toast.LENGTH_LONG
                                        ).show()
                                        navController.popBackStack()
                                    },
                                    onError = {
                                        Toast.makeText(
                                            context,
                                            "Fehler: $it",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                )
                            },
                            onError = { error ->
                                Toast.makeText(
                                    context,
                                    "Upload-Fehler: $error",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        )
                    }
                },
                enabled = imageUri != null && name.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Spraywall hochladen")
            }
        }
    }
}


fun uploadToNextcloudViaWebDAV(
    context: Context,
    uri: Uri,
    onSuccess: (uploadedPreviewUrl: String) -> Unit,
    onError: (String) -> Unit
) {
    val username = "webupload"
    val appPassword = "eS6Ai-Bi8Lj-zA66p-SWPMC-qm8RL"
    val targetFolder = "uploads/images"

    val sharedToken = "ciKasyEBMQWANj7" // <- das ist der öffentliche Link-Token zur cloud!!!!

    val contentResolver = context.contentResolver
    val inputStream = contentResolver.openInputStream(uri) ?: run {
        onError("Datei konnte nicht geöffnet werden.")
        return
    }

    val mimeType = contentResolver.getType(uri) ?: "image/jpeg"
    val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType) ?: "jpg"
    val fileName = "${UUID.randomUUID()}.$extension"

    val bitmap = BitmapFactory.decodeStream(inputStream)
    val outputStream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
    val imageBytes = outputStream.toByteArray()

    val requestBody = imageBytes.toRequestBody("image/jpeg".toMediaTypeOrNull())
    val uploadUrl = "https://leitln.at/maltacloud/remote.php/dav/files/$username/$targetFolder/$fileName"
    val previewUrl = "https://leitln.at/maltacloud/index.php/s/$sharedToken/preview?file=/$fileName"

    val credential = Credentials.basic(username, appPassword)

    val request = Request.Builder()
        .url(uploadUrl)
        .put(requestBody)
        .header("Authorization", credential)
        .build()

    OkHttpClient().newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            onError("Netzwerkfehler: ${e.message}")
        }

        override fun onResponse(call: Call, response: Response) {
            if (response.isSuccessful) {
                createPublicShareLink(
                    fileName = fileName,
                    onSuccess = { publicPreviewUrl ->
                        onSuccess(publicPreviewUrl)
                    },
                    onError = { error ->
                        onError("Upload war erfolgreich, aber Teilen fehlgeschlagen: $error")
                    }
                )

            } else {
                onError("Fehler: ${response.code} - ${response.message}")
            }
        }
    })
}

fun createPublicShareLink(
    fileName: String,
    onSuccess: (String) -> Unit,
    onError: (String) -> Unit
) {
    val client = OkHttpClient()
    val url = "https://leitln.at/maltacloud/ocs/v2.php/apps/files_sharing/api/v1/shares"
    val requestBody = FormBody.Builder()
        .add("path", "/uploads/images/$fileName")
        .add("shareType", "3") // public link
        .add("permissions", "1") // read-only
        .build()

    val credential = Credentials.basic("webupload", "eS6Ai-Bi8Lj-zA66p-SWPMC-qm8RL")

    val request = Request.Builder()
        .url(url)
        .post(requestBody)
        .addHeader("Authorization", credential)
        .addHeader("OCS-APIREQUEST", "true") // required!
        .build()

    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            onError("Teilen fehlgeschlagen: ${e.message}")
        }

        override fun onResponse(call: Call, response: Response) {
            val body = response.body?.string()
            if (!response.isSuccessful || body == null) {
                onError("Share-Link fehlgeschlagen: ${response.code}")
                return
            }

            val token = Regex("<url>.*?/s/(.*)</url>").find(body)?.groupValues?.get(1)
            if (token != null) {
                val publicPreview = "https://leitln.at/maltacloud/index.php/s/$token/preview"
                onSuccess(publicPreview)
            } else {
                onError("Kein Share-Token gefunden.")
            }
        }
    })
}


