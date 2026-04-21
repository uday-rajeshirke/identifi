package com.ud23.identifi.presentation.screens

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.ud23.identifi.presentation.theme.IdentifiTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            IdentifiTheme {
                // Keep track of whether we have permission at the top level
                var hasPermission by remember { mutableStateOf(false) }

                if (hasPermission) {
                    // Start the AI Orchestrator!
                    FaceRecognitionScreen()
                } else {
                    // Show our custom rationale screen
                    CameraPermissionScreen(
                        onPermissionGranted = { hasPermission = true }
                    )
                }
            }
        }
    }
}