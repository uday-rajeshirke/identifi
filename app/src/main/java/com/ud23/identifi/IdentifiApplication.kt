package com.ud23.identifi

import android.app.Application
import com.ud23.identifi.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class IdentifiApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@IdentifiApplication)
            modules(appModule)
        }
    }
}