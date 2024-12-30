package com.example.travellupa.database

import android.net.Uri

sealed class Screen(val route: String) {
    object Greeting : Screen("greeting")
    object Login : Screen("login")
    object RekomendasiTempat : Screen("rekomendasi_tempat")
    object Gallery : Screen("gallery")
    object ImageDetail : Screen("imageDetail/{imagePath}") {
        fun createRoute(imagePath: String): String {
            return "imageDetail/${Uri.encode(imagePath)}"
        }
    }
}

data class TempatWisata(
    val nama: String = "",
    val deskripsi: String = "",
    val gambarUriString: String? = null,
    val gambarResId: Int? = null,
)