package com.ud23.identifi.di

import androidx.room.Room
import com.ud23.identifi.data.local.AppDatabase
import com.ud23.identifi.data.local.UserRepositoryImpl
import com.ud23.identifi.data.vision.FaceRecognitionRepositoryImpl
import com.ud23.identifi.domain.repository.FaceRecognitionRepository
import com.ud23.identifi.domain.repository.UserRepository
import com.ud23.identifi.domain.usecase.IdentifyFaceUseCase
import com.ud23.identifi.presentation.orchestrator.FaceProcessingOrchestrator
import com.ud23.identifi.presentation.viewmodel.FaceRecognitionViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {

    // 1. Database instances (Singletons)
    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "identifi_database"
        ).build()
    }
    single { get<AppDatabase>().userDao() }

    // 2. Repositories (Singletons)
    single<UserRepository> { UserRepositoryImpl(get()) }
    single<FaceRecognitionRepository> { FaceRecognitionRepositoryImpl(androidContext()) }

    // 3. Use Cases (Factories - recreated when needed)
    factory { IdentifyFaceUseCase(get(), get()) }

    // 4. Orchestrator (Singleton - we only want ONE pipeline managing the lock)
    single { FaceProcessingOrchestrator(get(), get()) }

    // 5. ViewModels
    viewModel { FaceRecognitionViewModel(get(), get()) }

}