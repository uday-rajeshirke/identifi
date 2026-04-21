package com.ud23.identifi.presentation.orchestrator

import com.ud23.identifi.domain.model.User

sealed class OrchestratorResult {
    data class UserIdentified(val user: User) : OrchestratorResult()
    data class UnknownFace(val faceVector: FloatArray) : OrchestratorResult()

    object NoFaceDetected : OrchestratorResult()
    object FrameDropped : OrchestratorResult() // Backpressure state
    data class Error(val exception: Throwable) : OrchestratorResult()
}