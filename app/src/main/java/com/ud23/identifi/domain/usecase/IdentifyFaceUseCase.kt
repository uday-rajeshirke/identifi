package com.ud23.identifi.domain.usecase

import com.ud23.identifi.domain.model.User
import com.ud23.identifi.domain.repository.FaceRecognitionRepository
import com.ud23.identifi.domain.repository.UserRepository
import kotlinx.coroutines.flow.first

class IdentifyFaceUseCase(
    private val faceRecognitionRepository: FaceRecognitionRepository,
    private val userRepository: UserRepository
) {
    private val SIMILARITY_THRESHOLD = 0.75f

    suspend operator fun invoke(liveVector: FloatArray): User? {
        val allUsers = userRepository.getAllUsers().first()

        var bestMatch: User? = null
        var highestScore = 0f

        for (user in allUsers) {
            val score = faceRecognitionRepository.calculateSimilarity(liveVector, user.faceVector)
            if (score > highestScore && score >= SIMILARITY_THRESHOLD) {
                highestScore = score
                bestMatch = user
            }
        }

        return bestMatch
    }
}