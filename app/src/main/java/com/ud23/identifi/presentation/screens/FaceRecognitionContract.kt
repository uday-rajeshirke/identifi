package com.ud23.identifi.presentation.screens

import android.graphics.Bitmap
import com.ud23.identifi.core.mvi.ViewEffect
import com.ud23.identifi.core.mvi.ViewIntent
import com.ud23.identifi.core.mvi.ViewState
import com.ud23.identifi.domain.model.User

class FaceRecognitionContract {

    sealed class Intent : ViewIntent {
        data class ProcessCameraFrame(val bitmap: Bitmap) : Intent()
        data class RegisterNewUser(val name: String, val faceVector: FloatArray) : Intent()
        object ResetState : Intent() // To clear the screen after a recognition
    }

    sealed class State : ViewState {
        object Idle : State()
        object Scanning : State()
        data class UserRecognized(val user: User) : State()
        data class UnknownFaceDetected(val faceVector: FloatArray) : State()
        data class Error(val message: String) : State()
    }

    sealed class Effect : ViewEffect {
        data class ShowToast(val message: String) : Effect()
    }
}