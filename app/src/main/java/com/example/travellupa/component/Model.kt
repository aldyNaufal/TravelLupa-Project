package com.example.travellupa.component

import android.content.Context
import android.net.Uri
import android.util.Log
import java.io.File


fun saveImageLocally(context: Context, uri: Uri): String {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri)
        val file = File(context.filesDir, "image_${System.currentTimeMillis()}.jpg")
        inputStream?.use { input ->
            file.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        Log.d("ImageSave", "Image saved successfully to: ${file.absolutePath}")
        file.absolutePath
    } catch (e: Exception) {
        Log.e("ImageSave", "Error saving image", e)
        throw e
    }
}





