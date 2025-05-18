package com.mountainlabs.musix

import android.app.Application
import com.mountainlabs.musix.dependencyInjection.appModule
import org.koin.core.context.startKoin

class App : Application(){
    override fun onCreate() {
        super.onCreate()
        startKoin {
            modules(appModule)
        }
    }
}