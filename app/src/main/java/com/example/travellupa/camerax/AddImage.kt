package com.example.travellupa.camerax

import androidx.compose.material3.Icon
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import coil.compose.rememberAsyncImagePainter
import androidx.compose.material3.*


@Composable
fun AddImageDialog(
    onDismiss: () -> Unit,
    onImageAdded: (Uri, String?) -> Unit
) {
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var location by remember { mutableStateOf("") }
    var showCameraView by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // Launcher untuk galeri
    val imagePickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        imageUri = uri
    }

    // Launcher untuk permission kamera
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            showCameraView = true
        } else {
            Toast.makeText(
                context,
                "Camera permission is required",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    if (showCameraView) {
        CameraView(
            onImageCaptured = { uri ->
                imageUri = uri
                showCameraView = false
            },
            onClose = {
                showCameraView = false
            }
        )
    } else {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Add New Image") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    imageUri?.let { uri ->
                        Image(
                            painter = rememberAsyncImagePainter(model = uri),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it },
                        label = { Text("Location") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Tombol Gallery
                    FilledTonalButton(
                        onClick = { imagePickerLauncher.launch("image/*") },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Select from Gallery")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Tombol Camera
                    FilledTonalButton(
                        onClick = {
                            cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Take Photo")
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        imageUri?.let { uri ->
                            onImageAdded(uri, location)
                            onDismiss()
                        }
                    },
                    enabled = imageUri != null
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        )
    }
}
