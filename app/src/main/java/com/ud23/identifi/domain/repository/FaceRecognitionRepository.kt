package com.ud23.identifi.domain.repository

import android.graphics.Bitmap

interface FaceRecognitionRepository {
    suspend fun extractFaceEmbedding(bitmap: Bitmap): FloatArray?
    fun calculateSimilarity(vectorA: FloatArray, vectorB: FloatArray): Float
}