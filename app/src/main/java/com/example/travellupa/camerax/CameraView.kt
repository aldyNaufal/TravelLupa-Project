package com.example.travellupa.camerax

import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.Surface
import android.view.WindowManager
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.travellupa.R
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import kotlin.coroutines.resume

@Composable
fun CameraView(
    onImageCaptured: (Uri) -> Unit,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val rotation = context.getSystemService(Context.WINDOW_SERVICE)
        ?.let { it as WindowManager }
        ?.defaultDisplay
        ?.rotation
        ?: Surface.ROTATION_0

    var lensFacing by remember { mutableStateOf(CameraSelector.DEFAULT_BACK_CAMERA) }
    val preview = remember {
        Preview.Builder()
            .setTargetRotation(rotation)
            .build()
    }
    val imageCapture = remember {
        ImageCapture.Builder()
            .setTargetRotation(rotation)
            .build()
    }

    var cameraProvider by remember { mutableStateOf<ProcessCameraProvider?>(null) }

    LaunchedEffect(Unit) {
        cameraProvider = suspendCancellableCoroutine { continuation ->
            val providerFuture = ProcessCameraProvider.getInstance(context)
            providerFuture.addListener({
                continuation.resume(providerFuture.get())
            }, ContextCompat.getMainExecutor(context))
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Camera preview
        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).apply {
                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                }
            },
            modifier = Modifier.fillMaxSize(),
            update = { previewView ->
                cameraProvider?.let { provider ->
                    try {
                        provider.unbindAll()
                        provider.bindToLifecycle(
                            lifecycleOwner,
                            lensFacing,
                            preview,
                            imageCapture
                        )
                        preview.setSurfaceProvider(previewView.surfaceProvider)
                    } catch (exc: Exception) {
                        Log.e("CameraView", "Use case binding failed", exc)
                    }
                }
            }
        )

        // Close button (top-left corner)
        IconButton(
            onClick = onClose,
            modifier = Modifier
                .size(48.dp)
                .padding(16.dp)
                .align(Alignment.TopStart)
                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.close_icon),
                contentDescription = "Close",
                tint = Color.White,
                modifier = Modifier.size(25.dp)
            )
        }

        // Take photo button (bottom-center)
        IconButton(
            onClick = {
                val photoFile = File(
                    context.cacheDir,
                    "photo_${System.currentTimeMillis()}.jpg"
                )

                val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
                imageCapture.takePicture(
                    outputOptions,
                    ContextCompat.getMainExecutor(context),
                    object : ImageCapture.OnImageSavedCallback {
                        override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                            onImageCaptured(Uri.fromFile(photoFile))
                        }

                        override fun onError(exception: ImageCaptureException) {
                            Log.e("CameraView", "Photo capture failed: ${exception.message}", exception)
                        }
                    }
                )
            },
            modifier = Modifier
                .size(64.dp)
                .align(Alignment.BottomCenter)
                .padding(16.dp)
                .background(Color.White, CircleShape)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.photo_icon),
                contentDescription = "Take photo",
                tint = Color.Black,
                modifier = Modifier.size(25.dp)
            )
        }

        // Flip camera button (bottom-left corner)
        IconButton(
            onClick = {
                lensFacing = if (lensFacing == CameraSelector.DEFAULT_FRONT_CAMERA) {
                    CameraSelector.DEFAULT_BACK_CAMERA
                } else {
                    CameraSelector.DEFAULT_FRONT_CAMERA
                }
            },
            modifier = Modifier
                .size(48.dp)
                .padding(16.dp)
                .align(Alignment.BottomStart)
                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.flip_icon),
                contentDescription = "Flip camera",
                tint = Color.White,
                modifier = Modifier.size(25.dp)
            )
        }
    }
}
