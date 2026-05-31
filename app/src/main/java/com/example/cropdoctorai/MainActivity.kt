package com.example.cropdoctorai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.cropdoctorai.navigation.AppNavigation
import com.example.cropdoctorai.ui.theme.CropDoctorAITheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Main activity — entry point for the CropDoctor.AI app.
 * Annotated with @AndroidEntryPoint for Hilt dependency injection.
 * Hosts the Compose navigation graph with edge-to-edge rendering.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CropDoctorAITheme {
                AppNavigation()
            }
        }
    }
}