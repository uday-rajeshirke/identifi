package com.ud23.identifi.presentation.orchestrator

import android.graphics.Bitmap
import com.ud23.identifi.domain.repository.FaceRecognitionRepository
import com.ud23.identifi.domain.usecase.IdentifyFaceUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicBoolean

class FaceProcessingOrchestrator(
    private val faceRecognitionRepository: FaceRecognitionRepository,
    private val identifyFaceUseCase: IdentifyFaceUseCase
) {
    private val isProcessing = AtomicBoolean(false)

    suspend fun processFrame(bitmap: Bitmap): OrchestratorResult =
        withContext(Dispatchers.Default) {
            // BACKPRESSURE: If we are currently processing, immediately drop the frame
            if (!isProcessing.compareAndSet(false, true)) {
                return@withContext OrchestratorResult.FrameDropped
            }

            try {
                // Step 1: Extract Face Vector using MediaPipe
                val faceVector = faceRecognitionRepository.extractFaceEmbedding(bitmap)

                if (faceVector == null) {
                    return@withContext OrchestratorResult.NoFaceDetected
                }

                // Step 2: Compare against Room Database
                val identifiedUser = identifyFaceUseCase(faceVector)

                // Step 3: Return the final state
                if (identifiedUser != null) {
                    OrchestratorResult.UserIdentified(identifiedUser)
                } else {
                    OrchestratorResult.UnknownFace(faceVector)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                OrchestratorResult.Error(e)
            } finally {
                // ALWAYS release the lock when finished or if it crashes
                isProcessing.set(false)
            }
        }
}