package com.example.travellupa.component

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Card
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.travellupa.R
import com.example.travellupa.database.ImageDao
import com.example.travellupa.database.TempatWisata
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun TempatItemEditable(
    tempat: TempatWisata,
    imageDao: ImageDao,
    scope: CoroutineScope,
    onDelete: () -> Unit
) {
    val firestore = FirebaseFirestore.getInstance()
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .background(MaterialTheme.colorScheme.surface),
        elevation = 4.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Image(
                painter = tempat.gambarUriString?.let { uriString ->
                    rememberAsyncImagePainter(
                        ImageRequest.Builder(LocalContext.current)
                            .data(Uri.parse(uriString))
                            .build()
                    )
                } ?: painterResource(id = R.drawable.default_image),
                contentDescription = tempat.nama,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentScale = ContentScale.Crop
            )
            Box(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Text(
                        text = tempat.nama,
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(bottom = 8.dp, top = 12.dp)
                    )
                    Text(
                        text = tempat.deskripsi,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                IconButton(
                    onClick = { expanded = true },
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Icon(Icons.Default.MoreVert, contentDescription = "More options")
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DropdownMenuItem(onClick = {
                        expanded = false

                        // Hapus data dari Firestore
                        firestore.collection("tempat_wisata").document(tempat.nama)
                            .delete()
                            .addOnSuccessListener {
                                // Hapus data dari Room Database setelah berhasil dari Firestore
                                scope.launch(Dispatchers.IO) {
                                    val localImage = imageDao.getImageByTempatWisataId(tempat.nama)
                                    localImage?.let {
                                        imageDao.delete(it)
                                        // Hapus file lokal (gambar)
                                        File(it.localPath).delete()
                                    }

                                    // Panggil callback untuk memperbarui UI
                                    withContext(Dispatchers.Main) {
                                        onDelete()
                                    }
                                }
                            }
                            .addOnFailureListener { e ->
                                Log.w("TempatItemEditable", "Error deleting document", e)
                            }
                    }) {
                        Text("Delete")
                    }
                }
            }
        }
    }
}

