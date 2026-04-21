package com.ud23.identifi.presentation.screens

import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.ud23.identifi.presentation.viewmodel.FaceRecognitionViewModel
import org.koin.androidx.compose.koinViewModel
import java.util.concurrent.Executors

@Composable
fun FaceRecognitionScreen(
    viewModel: FaceRecognitionViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    val state by viewModel.state.collectAsState()

    // Handle One-Time Effects (like Toasts)
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is FaceRecognitionContract.Effect.ShowToast -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // --- 1. Camera Preview Background ---
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx)
                val cameraExecutor = Executors.newSingleThreadExecutor()
                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()

                    val preview = Preview.Builder().build().also {
                        it.surfaceProvider = previewView.surfaceProvider
                    }

                    val imageAnalyzer = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()
                        .also {
                            it.setAnalyzer(cameraExecutor, FaceImageAnalyzer { bitmap ->
                                // Fire Intent to ViewModel!
                                // (The Orchestrator will automatically drop frames if it's busy)
                                viewModel.handleIntent(
                                    FaceRecognitionContract.Intent.ProcessCameraFrame(
                                        bitmap
                                    )
                                )
                            })
                        }

                    // Use Front Camera for Face Recognition
                    val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner, cameraSelector, preview, imageAnalyzer
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }, ContextCompat.getMainExecutor(ctx))

                previewView
            },
            modifier = Modifier.fillMaxSize()
        )

        // --- 2. UI Overlay based on MVI State ---
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (val currentState = state) {
                is FaceRecognitionContract.State.Scanning -> {
                    CircularProgressIndicator(color = Color.White)
                    Text(
                        "Scanning AI Pipeline...",
                        color = Color.White,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                is FaceRecognitionContract.State.UserRecognized -> {
                    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF4CAF50))) {
                        Text(
                            text = "User: ${currentState.user.name}",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                    LaunchedEffect(Unit) {
                        kotlinx.coroutines.delay(3000)
                        viewModel.handleIntent(FaceRecognitionContract.Intent.ResetState)
                    }
                }

                is FaceRecognitionContract.State.UnknownFaceDetected -> {
                    RegistrationDialog(
                        onRegister = { name ->
                            viewModel.handleIntent(
                                FaceRecognitionContract.Intent.RegisterNewUser(
                                    name,
                                    currentState.faceVector
                                )
                            )
                        },
                        onDismiss = {
                            viewModel.handleIntent(FaceRecognitionContract.Intent.ResetState)
                        }
                    )
                }

                is FaceRecognitionContract.State.Error -> {
                    Text(
                        "Error: ${currentState.message}",
                        color = Color.Red,
                        fontWeight = FontWeight.Bold
                    )
                }

                FaceRecognitionContract.State.Idle -> {
                    // Do nothing, waiting for a face
                }
            }
        }
    }
}