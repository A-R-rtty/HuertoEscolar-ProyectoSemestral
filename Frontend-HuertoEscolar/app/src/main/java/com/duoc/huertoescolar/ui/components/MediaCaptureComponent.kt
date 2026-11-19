package com.duoc.huertoescolar.ui.components

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import java.io.File
import java.util.UUID

object MediaStorageHelper {
    fun crearArchivoEvidencia(context: Context): Pair<File, Uri> {
        val dir = File(context.filesDir, "evidencias").apply { if (!exists()) mkdirs() }
        val file = File(dir, "huerto_${UUID.randomUUID()}.jpg")
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        return file to uri
    }

    fun persistirUriEnInterno(context: Context, sourceUri: Uri): String? {
        return runCatching {
            val dir = File(context.filesDir, "evidencias").apply { if (!exists()) mkdirs() }
            val destFile = File(dir, "picker_${UUID.randomUUID()}.jpg")
            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                destFile.outputStream().use { output -> input.copyTo(output) }
            }
            Uri.fromFile(destFile).toString()
        }.getOrNull()
    }
}

@Composable
fun EvidenciaFotograficaSelector(
    uriActual: String?,
    onUriSeleccionada: (String?) -> Unit,
    titulo: String = "Evidencia fotográfica (plantas, cultivos o herramientas)"
) {
    val context = LocalContext.current
    var tempCameraFile by remember { mutableStateOf<File?>(null) }
    var mensajePermiso by remember { mutableStateOf<String?>(null) }

    // Launcher Photo Picker (Android 13+ / Backport Google Play Services)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val persistida = MediaStorageHelper.persistirUriEnInterno(context, uri)
            onUriSeleccionada(persistida)
            mensajePermiso = null
        }
    }

    // Launcher Cámara
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { exito: Boolean ->
        val file = tempCameraFile
        if (exito && file != null && file.exists()) {
            onUriSeleccionada(Uri.fromFile(file).toString())
            mensajePermiso = null
        } else {
            file?.delete()
        }
    }

    val lanzarCamara = {
        val (file, contentUri) = MediaStorageHelper.crearArchivoEvidencia(context)
        tempCameraFile = file
        cameraLauncher.launch(contentUri)
    }

    // Launcher Solicitud Permiso Cámara
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (concedido) {
            lanzarCamara()
        } else {
            mensajePermiso = "Permiso de cámara denegado. Puedes adjuntar una imagen desde la galería."
        }
    }

    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(titulo, style = MaterialTheme.typography.titleMedium)
            Text(
                "Nota de privacidad: Fotografiar únicamente cultivos, suelo o herramientas (sin rostros).",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = {
                        val tienePermiso = ContextCompat.checkSelfPermission(
                            context, Manifest.permission.CAMERA
                        ) == PackageManager.PERMISSION_GRANTED
                        if (tienePermiso) lanzarCamara()
                        else permissionLauncher.launch(Manifest.permission.CAMERA)
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("Cámara")
                }

                OutlinedButton(
                    onClick = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("Galería")
                }
            }

            mensajePermiso?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
            }

            if (!uriActual.isNullOrBlank()) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    AsyncImage(
                        model = uriActual,
                        contentDescription = "Vista previa de evidencia",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(10.dp))
                    )
                    FilledTonalIconButton(
                        onClick = { onUriSeleccionada(null) },
                        modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Quitar imagen")
                    }
                }
            }
        }
    }
}