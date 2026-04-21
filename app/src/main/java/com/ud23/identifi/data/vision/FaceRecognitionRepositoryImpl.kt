package com.ud23.identifi.data.vision

import android.content.Context
import android.graphics.Bitmap
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.imageembedder.ImageEmbedder
import com.ud23.identifi.domain.repository.FaceRecognitionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.sqrt

class FaceRecognitionRepositoryImpl(
    private val context: Context
) : FaceRecognitionRepository {

    private var imageEmbedder: ImageEmbedder? = null

    init {
        setupEmbedder()
    }

    private fun setupEmbedder() {
        val baseOptions = BaseOptions.builder()
            .setModelAssetPath("mobile_facenet.tflite")
            .build()

        val options = ImageEmbedder.ImageEmbedderOptions.builder()
            .setBaseOptions(baseOptions)
            .build()

        imageEmbedder = ImageEmbedder.createFromOptions(context, options)
    }

    override suspend fun extractFaceEmbedding(bitmap: Bitmap): FloatArray? {
        return withContext(Dispatchers.Default) {
            try {
                val mpImage = BitmapImageBuilder(bitmap).build()
                val result = imageEmbedder?.embed(mpImage)

                // Extract the float array from the MediaPipe result
                result?.embeddingResult()?.embeddings()?.firstOrNull()?.floatEmbedding()
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