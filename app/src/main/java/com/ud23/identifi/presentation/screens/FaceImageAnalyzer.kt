package com.ud23.identifi.presentation.screens

import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy

class FaceImageAnalyzer(
    private val onFrameExtracted: (Bitmap) -> Unit
) : ImageAnalysis.Analyzer {

    @androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        // Modern CameraX has a built-in toBitmap() function!
        val bitmap = imageProxy.toBitmap()

        // The camera sensor might be rotated, so we rotate the bitmap to match the screen
        val rotationDegrees = imageProxy.imageInfo.rotationDegrees.toFloat()
        val matrix = Matrix().apply { postRotate(rotationDegrees) }

        val rotatedBitmap = Bitmap.createBitmap(
            bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true
        )

        // Send it to the ViewModel
        onFrameExtracted(rotatedBitmap)

        // CRITICAL: You must close the proxy so CameraX can send the next frame
        imageProxy.close()
    }
}