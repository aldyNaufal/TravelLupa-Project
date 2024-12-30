package com.example.travellupa.component

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.TextField
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.travellupa.database.AppDatabase
import com.example.travellupa.database.ImageDao
import com.example.travellupa.database.ImageEntity
import com.example.travellupa.database.TempatWisata
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.UUID

@Composable
fun TambahTempatWisataDialog(
    firestore: FirebaseFirestore,
    imageDao: ImageDao,
    context: Context,
    scope: CoroutineScope,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit
) {
    var nama by remember { mutableStateOf("") }
    var deskripsi by remember { mutableStateOf("") }
    var gambarUri by remember { mutableStateOf<Uri?>(null) }
    var isUploading by remember { mutableStateOf(false) }

    val gambarLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            // Salin URI yang dipilih ke variabel state
            gambarUri = it
            Log.d("ImagePicker", "Selected URI: $it")
        }
    }

    // Function to save image to internal storage
    suspend fun saveImageToInternalStorage(uri: Uri): String {
        return withContext(Dispatchers.IO) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                    ?: throw IOException("Cannot open input stream")

                val fileName = "img_${System.currentTimeMillis()}.jpg"
                val file = File(context.filesDir, fileName)

                FileOutputStream(file).use { outputStream ->
                    inputStream.use { input ->
                        input.copyTo(outputStream)
                    }
                }

                Log.d("ImageSave", "Image saved successfully at: ${file.absolutePath}")
                file.absolutePath
            } catch (e: Exception) {
                Log.e("ImageSave", "Error saving image", e)
                throw e
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tambah Tempat Wisata Baru") },
        text = {
            Column {
                TextField(
                    value = nama,
                    onValueChange = { nama = it },
                    label = { Text("Nama Tempat") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isUploading
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = deskripsi,
                    onValueChange = { deskripsi = it },
                    label = { Text("Deskripsi") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isUploading
                )
                Spacer(modifier = Modifier.height(8.dp))
                gambarUri?.let { uri ->
                    Image(
                        painter = rememberAsyncImagePainter(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(uri)
                                .crossfade(true)
                                .build()
                        ),
                        contentDescription = "Gambar yang dipilih",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentScale = ContentScale.Crop
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { gambarLauncher.launch("image/*") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isUploading
                ) {
                    Text("Pilih Gambar")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (nama.isNotBlank() && deskripsi.isNotBlank()) {
                        isUploading = true
                        scope.launch {
                            try {
                                // Save image if selected
                                val localImagePath = gambarUri?.let { uri ->
                                    try {
                                        val path = saveImageToInternalStorage(uri)
                                        Log.d("ImageSave", "Local path: $path")

                                        // Gunakan Dispatchers.IO untuk operasi database
                                        withContext(Dispatchers.IO) {
                                            val imageEntity = ImageEntity(
                                                localPath = path,
                                                tempatWisataId = nama,
                                                createdAt = System.currentTimeMillis()
                                            )
                                            imageDao.insert(imageEntity)
                                        }
                                        path
                                    } catch (e: Exception) {
                                        Log.e("ImageSave", "Failed to save image", e)
                                        null
                                    }
                                }

                                // Save to Firestore
                                val tempatWisata = TempatWisata(
                                    nama = nama,
                                    deskripsi = deskripsi,
                                    gambarUriString = localImagePath
                                )

                                Log.d("Firestore", "Saving tempat wisata: $tempatWisata")

                                firestore.collection("tempat_wisata")
                                    .document(nama)
                                    .set(tempatWisata)
                                    .await()

                                withContext(Dispatchers.Main) {
                                    onSuccess()
                                }
                            } catch (e: Exception) {
                                Log.e("TambahDialog", "Error saving data", e)
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(
                                        context,
                                        "Error: ${e.message}",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            } finally {
                                isUploading = false
                            }
                        }
                    }
                    },
                enabled = !isUploading && nama.isNotBlank() && deskripsi.isNotBlank()
            ) {
                if (isUploading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White
                    )
                } else {
                    Text("Tambah")
                }
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.surface),
                enabled = !isUploading
            ) {
                Text("Batal")
            }
        }
    )
}
