package com.example.travellupa

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.google.firebase.FirebaseApp
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Surface
import com.example.travellupa.ui.theme.TravelLupaTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.example.travellupa.navigation.AppNavigation



class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        val currentUser: FirebaseUser? = FirebaseAuth.getInstance().currentUser

        setContent {
            TravelLupaTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.White
                ) {
                    AppNavigation(currentUser)
                }
            }
        }
    }
}


















