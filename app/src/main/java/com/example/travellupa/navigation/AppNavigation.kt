package com.example.travellupa.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.travellupa.camerax.ImageDaoProvider
import com.example.travellupa.database.Screen
import com.example.travellupa.screen.GalleryScreen
import com.example.travellupa.screen.GreetingScreen
import com.example.travellupa.screen.LoginScreen
import com.example.travellupa.screen.RekomendasiTempatScreen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun AppNavigation(currentUser: FirebaseUser?) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = if (currentUser != null) Screen.RekomendasiTempat.route else Screen.Greeting.route
    ) {
        composable(Screen.Greeting.route) {
            GreetingScreen(
                onStart = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Greeting.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.RekomendasiTempat.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.RekomendasiTempat.route) { backStackEntry ->
            val firestore = FirebaseFirestore.getInstance()
            val context = LocalContext.current

            RekomendasiTempatScreen(
                firestore = firestore,
                context = context,
                navController = navController,
                onBackToLogin = {
                    FirebaseAuth.getInstance().signOut()

                    navController.navigate(Screen.Greeting.route) {
                        popUpTo(Screen.RekomendasiTempat.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Gallery.route) { backStackEntry ->
            val context = LocalContext.current
            GalleryScreen(
                tempatWisataId = null,
                context = context,
                onImageSelected = { uri ->
                    // Navigasi ke halaman detail gambar atau gunakan sesuai kebutuhan
                    navController.navigate(Screen.ImageDetail.createRoute(uri.toString()))
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }

    }
}