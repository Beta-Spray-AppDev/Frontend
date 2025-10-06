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
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.colorResource
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
import okio.BufferedSink
import okio.source
import com.example.sprayconnectapp.R


/**
 * Screen zum Anlegen einer Spraywall:
 * - Nutzer wählt ein Bild (Gallery-Picker) und gibt Metadaten ein
 * - Bild wird per WebDAV nach Nextcloud hochgeladen
 * - Nach erfolgreichem Upload wird ein öffentlicher Share-Link erzeugt
 * - Dieser Link wird als photoUrl in das SpraywallDTO geschrieben und an das Backend gesendet
 */


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSpraywallScreen(
    navController: NavController,
    gymId: String,
    gymName: String,
    viewModel: SpraywallViewModel = rememberSpraywallViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var uploadedImageUrl by remember { mutableStateOf<String?>(null) }

    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var isPublic by remember { mutableStateOf(true) }

    // UI State
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var infoMessage by remember { mutableStateOf<String?>(null) }



    // Farben
    val barColor = colorResource(id = R.color.hold_type_bar)

    val tfColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = Color(0xFF00796B),
        cursorColor = Color(0xFF00796B),
        focusedLabelColor = Color(0xFF00796B),
        unfocusedContainerColor = Color.White,
        focusedContainerColor = Color.White
    )


    val buttonColor = colorResource(R.color.button_normal)

    val switchColors = SwitchDefaults.colors(
        checkedThumbColor = Color.White,
        checkedTrackColor = buttonColor,
        uncheckedThumbColor = Color.White,
        uncheckedTrackColor = Color(0xFFE0E0E0)
    )
    val screenBg = Brush.verticalGradient(
        colors = listOf(Color(0xFF53535B), Color(0xFF767981), Color(0xFFA8ABB2))
    )

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri -> imageUri = uri }
    )

    fun canSave(): Boolean = !isLoading && imageUri != null && name.isNotBlank()


    fun saveSpraywall() {
        if (!canSave()) return
        isLoading = true
        errorMessage = null
        infoMessage = null

        val uri = imageUri!!
        uploadToNextcloudViaWebDAV(
            context = context,
            uri = uri,
            onSuccess = { uploadedUrl ->
                val dto = SpraywallDTO(
                    name = name.trim(),
                    description = description.trim(),
                    photoUrl = uploadedUrl,
                    isPublic = isPublic,
                    gymId = UUID.fromString(gymId),
                    isArchived = false
                )

                viewModel.createSpraywall(
                    context = context,
                    dto = dto,
                    onSuccess = {
                        isLoading = false
                        Toast.makeText(context, "Spraywall erfolgreich erstellt!", Toast.LENGTH_LONG).show()
                        navController.popBackStack()
                    },
                    onError = { err ->
                        isLoading = false
                        errorMessage = err
                    }
                )
            },
            onError = { err ->
                isLoading = false
                errorMessage = "Upload-Fehler: $err"
            }
        )
    }


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
                        containerColor = barColor,
                        scrolledContainerColor = barColor,
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White,
                        actionIconContentColor = Color.White
                    ),
                    title = { Text("Neue Spraywall – $gymName") },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Zurück")
                        }
                    }
                )
            }
        ) { inner ->
            Column(
                modifier = Modifier
                    .padding(inner)
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
                            "Spraywall anlegen",
                            style = MaterialTheme.typography.headlineSmall,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                        Divider()

                        // Bild
                        Button(
                            onClick = { launcher.launch("image/*") },
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colorResource(R.color.button_normal),
                                contentColor = Color.White
                            ),
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Icon(Icons.Default.Upload, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                if (imageUri == null) "Bild auswählen" else "Bild ändern"
                            )                        }

                        imageUri?.let { uri ->
                            AsyncImage(
                                model = uri,
                                contentDescription = "Gewähltes Bild",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .clip(RoundedCornerShape(12.dp)),
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                            )
                        }

                        // Name
                        OutlinedTextField(
                            value = name,
                            onValueChange = { if (it.length <= 20) name = it },
                            label = { Text("Name") },
                            placeholder = { Text("Name der Spraywall") },
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

                        // Beschreibung
                        OutlinedTextField(
                            value = description,
                            onValueChange = { if (it.length <= 20) description = it },
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
                                    saveSpraywall()
                                }
                            )
                        )

                        // Sichtbarkeit
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Switch(checked = isPublic, onCheckedChange = { isPublic = it }, colors = switchColors)
                            Spacer(Modifier.width(8.dp))
                            Text("Öffentlich sichtbar")
                        }

                        // Speichern
                        Button(
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colorResource(R.color.button_normal),
                                contentColor = Color.White
                            ),
                            onClick = { saveSpraywall() },
                            enabled = canSave(),
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .wrapContentWidth()
                        ) {
                            Text("Spraywall hochladen")
                        }

                        // Info/Fehler
                        infoMessage?.let { Text(it, color = MaterialTheme.colorScheme.primary) }
                        errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }

                        if (isLoading) {
                            CircularProgressIndicator(
                                color = colorResource(R.color.button_normal),
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                        }
                    }
                }
            }
        }
    }



}


/**
 * Lädt eine lokale Datei via WebDAV nach Nextcloud hoch.
 * - Bei Erfolg wird direkt ein öffentlicher Share-Link für die hochgeladene Datei erzeugt
 * - Rückgabe über onSuccess als Preview-URL (wird später für Bild-Download/Anzeige benutzt)
 */


fun uploadToNextcloudViaWebDAV(
    context: Context,
    uri: Uri,
    onSuccess: (uploadedPreviewUrl: String) -> Unit,
    onError: (String) -> Unit
) {
    val username = "webupload"
    val appPassword = "eS6Ai-Bi8Lj-zA66p-SWPMC-qm8RL"
    val targetFolder = "uploads/images"

    val mimeType = context.contentResolver.getType(uri) ?: "application/octet-stream"
    val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType) ?: "bin"
    val fileName = "${UUID.randomUUID()}.$extension"

    val requestBody = requestBodyFromUri(context, uri, mimeType)

    val uploadUrl = "https://leitln.at/maltacloud/remote.php/dav/files/$username/$targetFolder/$fileName"
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
                    onSuccess = { publicPreviewUrl -> onSuccess(publicPreviewUrl) },
                    onError = { error -> onError("Upload war erfolgreich, aber Teilen fehlgeschlagen: $error") }
                )
            } else {
                onError("Fehler: ${response.code} - ${response.message}")
            }
        }
    })
}


/**
 * Erzeugt über die Nextcloud-OCS-API einen öffentlichen Share-Link.
 * Gibt eine **Preview-URL** zurück (wird später zum Anzeigen/Herunterladen verwendet).
 */

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

/** RequestBody, der den Content der Content-URI in den HTTP-Body streamt. */


private fun requestBodyFromUri(context: Context, uri: Uri, mime: String): RequestBody =
    object : RequestBody() {
        override fun contentType() = mime.toMediaTypeOrNull()
        override fun writeTo(sink: BufferedSink) {
            context.contentResolver.openInputStream(uri)!!.use { input ->
                sink.writeAll(input.source())
            }
        }
    }


