package com.ud23.identifi.presentation.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ud23.identifi.domain.model.User
import com.ud23.identifi.domain.repository.UserRepository
import com.ud23.identifi.presentation.orchestrator.FaceProcessingOrchestrator
import com.ud23.identifi.presentation.orchestrator.OrchestratorResult
import com.ud23.identifi.presentation.screens.FaceRecognitionContract
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.util.UUID

class FaceRecognitionViewModel(
    private val orchestrator: FaceProcessingOrchestrator,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _state =
        MutableStateFlow<FaceRecognitionContract.State>(FaceRecognitionContract.State.Idle)
    val state: StateFlow<FaceRecognitionContract.State> = _state.asStateFlow()

    private val _effect = Channel<FaceRecognitionContract.Effect>()
    val effect = _effect.receiveAsFlow()

    fun handleIntent(intent: FaceRecognitionContract.Intent) {
        when (intent) {
            is FaceRecognitionContract.Intent.ProcessCameraFrame -> processFrame(intent.bitmap)
            is FaceRecognitionContract.Intent.RegisterNewUser -> registerUser(
                intent.name,
                intent.faceVector
            )

            is FaceRecognitionContract.Intent.ResetState -> resetState()
        }
    }

    private fun processFrame(bitmap: Bitmap) {
        // If we are already showing a recognized user or registration prompt, ignore new frames
        if (_state.value is FaceRecognitionContract.State.UserRecognized ||
            _state.value is FaceRecognitionContract.State.UnknownFaceDetected
        ) {
            return
        }

        viewModelScope.launch {
            _state.value = FaceRecognitionContract.State.Scanning

            val result = orchestrator.processFrame(bitmap)

            when (result) {
                is OrchestratorResult.UserIdentified -> {
                    _state.value = FaceRecognitionContract.State.UserRecognized(result.user)
                }

                is OrchestratorResult.UnknownFace -> {
                    // Pass the vector to the UI so the user can register this face
                    _state.value =
                        FaceRecognitionContract.State.UnknownFaceDetected(result.faceVector)
                }

                is OrchestratorResult.Error -> {
                    _state.value = FaceRecognitionContract.State.Error(
                        result.exception.message ?: "Unknown Error"
                    )
                    _effect.send(FaceRecognitionContract.Effect.ShowToast("Processing Failed"))
                }

                OrchestratorResult.FrameDropped,
                OrchestratorResult.NoFaceDetected -> {
                    // Silently go back to idle to wait for the next frame
                    _state.value = FaceRecognitionContract.State.Idle
                }
            }
        }
    }

    private fun registerUser(name: String, faceVector: FloatArray) {
        viewModelScope.launch {
            try {
                val newUser = User(
                    id = UUID.randomUUID().toString(),
                    name = name,
                    faceVector = faceVector
                )
                userRepository.saveUser(newUser)

                _effect.send(FaceRecognitionContract.Effect.ShowToast("User $name Registered!"))
                resetState()
            } catch (e: Exception) {
                _state.value = FaceRecognitionContract.State.Error("Registration Failed")
            }
        }
    }

    private fun resetState() {
        _state.value = FaceRecognitionContract.State.Idle
    }
}