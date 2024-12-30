package com.example.travellupa.screen

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.room.Room
import com.example.travellupa.R
import com.example.travellupa.component.TambahTempatWisataDialog
import com.example.travellupa.component.TempatItemEditable
import com.example.travellupa.database.AppDatabase
import com.example.travellupa.database.DatabaseInstance
import com.example.travellupa.database.ImageDao
import com.example.travellupa.database.ImageEntity
import com.example.travellupa.database.TempatWisata
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

@Composable
fun RekomendasiTempatScreen(
    firestore: FirebaseFirestore,
    context: Context,
    onBackToLogin: () -> Unit,
    navController: NavHostController, // Tambahkan NavController untuk navigasi
) {
    val db = DatabaseInstance.getDatabase(context)
    val imageDao = db.imageDao()


    var daftarTempatWisata by remember { mutableStateOf<List<TempatWisata>>(emptyList()) }
    var showTambahDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        firestore.collection("tempat_wisata")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("RekomendasiScreen", "Error listening to Firestore changes", e)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val firestoreData = snapshot.documents.mapNotNull { doc ->
                        try {
                            TempatWisata(
                                nama = doc.getString("nama") ?: "",
                                deskripsi = doc.getString("deskripsi") ?: "",
                                gambarUriString = doc.getString("gambarUriString")
                            )
                        } catch (e: Exception) {
                            Log.e("RekomendasiScreen", "Error parsing document", e)
                            null
                        }
                    }

                    scope.launch(Dispatchers.IO) {
                        val updatedData = firestoreData.map { tempat ->
                            val localImage = imageDao.getImageByTempatWisataId(tempat.nama)
                            tempat.copy(
                                gambarUriString = localImage?.localPath ?: tempat.gambarUriString
                            )
                        }

                        withContext(Dispatchers.Main) {
                            daftarTempatWisata = updatedData
                        }
                    }
                }
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Rekomendasi Tempat") },
                navigationIcon = {
                    IconButton(onClick = onBackToLogin) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                if (daftarTempatWisata.isEmpty()) {
                    Text(
                        text = "No data added yet",
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    LazyColumn {
                        items(daftarTempatWisata) { tempat ->
                            TempatItemEditable(
                                tempat = tempat,
                                imageDao = imageDao,
                                scope = scope,
                                onDelete = {
                                    scope.launch(Dispatchers.IO) {
                                        imageDao.getImageByTempatWisataId(tempat.nama)?.let { imageEntity ->
                                            imageDao.delete(imageEntity)
                                            File(imageEntity.localPath).delete()
                                        }
                                        firestore.collection("tempat_wisata").document(tempat.nama)
                                            .delete()
                                            .addOnSuccessListener {
                                                scope.launch(Dispatchers.Main) {
                                                    daftarTempatWisata =
                                                        daftarTempatWisata.filter { it.nama != tempat.nama }
                                                }
                                            }
                                            .addOnFailureListener { e ->
                                                Log.w("RekomendasiScreen", "Error deleting document", e)
                                            }
                                    }
                                }
                            )
                        }
                    }
                }
            }

            // Ikon Kamera di Pojok Kiri Bawah
            FloatingActionButton(
                onClick = {
                    navController.navigate("gallery") // Navigasi ke GalleryScreen
                },
                backgroundColor = Color(0xFFADD8E6), // Biru muda
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.camera_icon),
                    contentDescription = "Buka Galeri",
                    tint = Color.White,
                    modifier = Modifier.size(25.dp) // Ukuran ikon 10 dp
                )
            }

            // Ikon Tambah di Pojok Kanan Bawah
            FloatingActionButton(
                onClick = { showTambahDialog = true },
                backgroundColor = Color(0xFFADD8E6), // Biru muda
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                Icon(
                    Icons.Filled.Add,
                    contentDescription = "Tambah Tempat Wisata",
                    tint = Color.Black,
                    modifier = Modifier.size(25.dp) // Ukuran ikon 10 dp
                )
            }
        }

        if (showTambahDialog) {
            TambahTempatWisataDialog(
                firestore = firestore,
                imageDao = imageDao,
                context = context,
                scope = scope,
                onDismiss = { showTambahDialog = false },
                onSuccess = {
                    showTambahDialog = false
                }
            )
        }
    }
}

