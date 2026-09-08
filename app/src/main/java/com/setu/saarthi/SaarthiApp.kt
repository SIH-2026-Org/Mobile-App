package com.setu.saarthi

import android.app.Application
import com.setu.saarthi.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class SaarthiApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger(Level.ERROR)
            androidContext(this@SaarthiApp)
            modules(appModule)
        }
    }
}
