package com.example.travellupa.screen

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.AlertDialog
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.room.Room
import coil.compose.rememberAsyncImagePainter
import com.example.travellupa.camerax.AddImageDialog
import com.example.travellupa.camerax.ImageDetailDialog
import com.example.travellupa.component.saveImageLocally
import com.example.travellupa.database.AppDatabase
import com.example.travellupa.database.DatabaseInstance
import com.example.travellupa.database.ImageDao
import com.example.travellupa.database.ImageEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun GalleryScreen(
    tempatWisataId: String?,
    onImageSelected: (Uri) -> Unit,
    onBack: () -> Unit,
    context: Context
) {
    val db = DatabaseInstance.getDatabase(context)
    val imageDao = db.imageDao()

    val images by if (tempatWisataId.isNullOrEmpty()) {
        imageDao.getAllImages().collectAsState(initial = emptyList())
    } else {
        imageDao.getImageByTempatWisataIdFlow(tempatWisataId).collectAsState(initial = emptyList())
    }

    var selectedImageEntity by remember { mutableStateOf<ImageEntity?>(null) }
    var showAddImageDialog by remember { mutableStateOf(false) }
    val localContext = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gallery") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddImageDialog = true },
                backgroundColor = MaterialTheme.colors.primary
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add Image")
            }
        }
    ) { paddingValues ->
        LazyVerticalGrid(
            columns = GridCells.Adaptive(128.dp),
            contentPadding = PaddingValues(8.dp),
            modifier = Modifier.padding(paddingValues)
        ) {
            items(images) { image ->
                Image(
                    painter = rememberAsyncImagePainter(model = image.localPath),
                    contentDescription = null,
                    modifier = Modifier
                        .aspectRatio(1f)
                        .padding(4.dp)
                        .clickable {
                            selectedImageEntity = image  // Ini akan memicu tampilnya dialog
                        },
                    contentScale = ContentScale.Crop
                )
            }
        }

        if (showAddImageDialog) {
            AddImageDialog(
                onDismiss = { showAddImageDialog = false },
                onImageAdded = { uri, customLocation ->
                    val localPath = saveImageLocally(localContext, uri)
                    val newImage = ImageEntity(
                        localPath = localPath,
                        // Gunakan tempatWisataId jika ada, jika tidak gunakan customLocation
                        tempatWisataId = tempatWisataId ?: customLocation,
                        createdAt = System.currentTimeMillis()
                    )
                    CoroutineScope(Dispatchers.IO).launch {
                        imageDao.insert(newImage)
                    }
                    showAddImageDialog = false
                }
            )
        }

        selectedImageEntity?.let { imageEntity ->
            ImageDetailDialog(
                imageEntity = imageEntity,
                onDismiss = { selectedImageEntity = null },
                onDelete = { toDelete ->
                    CoroutineScope(Dispatchers.IO).launch {
                        imageDao.delete(toDelete)
                        File(toDelete.localPath).delete()
                    }
                    selectedImageEntity = null
                }
            )
        }
    }
}
