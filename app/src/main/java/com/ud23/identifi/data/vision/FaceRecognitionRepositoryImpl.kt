package com.ud23.identifi.data.vision

import android.content.Context
import android.graphics.Bitmap
import android.graphics.RectF
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.facedetector.FaceDetector
import com.google.mediapipe.tasks.vision.imageembedder.ImageEmbedder
import com.ud23.identifi.domain.repository.FaceRecognitionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.sqrt

class FaceRecognitionRepositoryImpl(
    private val context: Context
) : FaceRecognitionRepository {

    private var faceDetector: FaceDetector? = null
    private var imageEmbedder: ImageEmbedder? = null

    // REMOVED the init {} block! Koin will no longer crash on startup.

    private fun setupAI() {
        try {
            if (faceDetector == null) {
                val detectorOptions = FaceDetector.FaceDetectorOptions.builder()
                    .setBaseOptions(
                        BaseOptions.builder().setModelAssetPath("face_detection_short_range.tflite")
                            .build()
                    )
                    .build()
                faceDetector = FaceDetector.createFromOptions(context, detectorOptions)
            }

            if (imageEmbedder == null) {
                val embedderOptions = ImageEmbedder.ImageEmbedderOptions.builder()
                    .setBaseOptions(
                        BaseOptions.builder().setModelAssetPath("mobile_facenet.tflite").build()
                    )
                    .build()
                imageEmbedder = ImageEmbedder.createFromOptions(context, embedderOptions)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // If the files are missing, it will catch the error here instead of crashing the app
        }
    }

    override suspend fun extractFaceEmbedding(bitmap: Bitmap): FloatArray? {
        return withContext(Dispatchers.Default) {
            try {
                // LAZY LOAD: Initialize models on a background thread right before the first use
                if (faceDetector == null || imageEmbedder == null) {
                    setupAI()
                }

                // If they are STILL null, the files are missing from the assets folder!
                if (faceDetector == null || imageEmbedder == null) {
                    return@withContext null
                }

                val mpImage = BitmapImageBuilder(bitmap).build()

                // STEP 1: Find the Face
                val detectionResult = faceDetector?.detect(mpImage)
                val detections = detectionResult?.detections()

                if (detections.isNullOrEmpty()) {
                    return@withContext null // No face in the frame
                }

                val boundingBox: RectF = detections.first().boundingBox()

                // STEP 2: Calculate safe crop boundaries
                val left = boundingBox.left.toInt().coerceAtLeast(0)
                val top = boundingBox.top.toInt().coerceAtLeast(0)
                var width = boundingBox.width().toInt()
                var height = boundingBox.height().toInt()

                if (left + width > bitmap.width) width = bitmap.width - left
                if (top + height > bitmap.height) height = bitmap.height - top

                if (width <= 0 || height <= 0) return@withContext null

                // STEP 3: Crop the Bitmap to just the face
                val croppedFace = Bitmap.createBitmap(bitmap, left, top, width, height)
                val croppedMpImage = BitmapImageBuilder(croppedFace).build()

                // STEP 4: Extract the identity vector
                val embedderResult = imageEmbedder?.embed(croppedMpImage)
                embedderResult?.embeddingResult()?.embeddings()?.firstOrNull()?.floatEmbedding()

            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    override fun calculateSimilarity(vectorA: FloatArray, vectorB: FloatArray): Float {
        var dotProduct = 0.0f
        var normA = 0.0f
        var normB = 0.0f

        for (i in vectorA.indices) {
            dotProduct += vectorA[i] * vectorB[i]
            normA += vectorA[i] * vectorA[i]
            normB += vectorB[i] * vectorB[i]
        }

        return if (normA == 0.0f || normB == 0.0f) 0.0f else (dotProduct / (sqrt(normA) * sqrt(normB)))
    }
}