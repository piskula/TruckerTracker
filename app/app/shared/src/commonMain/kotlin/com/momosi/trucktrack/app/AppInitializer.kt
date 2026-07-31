package com.momosi.trucktrack.app

import coil3.SingletonImageLoader
import com.momosi.trucktrack.app.di.allModules
import com.momosi.trucktrack.core.common.crashreporting.CrashReporting
import com.momosi.trucktrack.core.common.environment.AppEnvironment
import com.momosi.trucktrack.core.common.logger.Logger
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin

fun initKoin(platformConfig: KoinApplication.() -> Unit = {}) {
    startKoin {
        platformConfig()
        modules(allModules)
    }
}

fun initApp(isDebug: Boolean) {
    AppEnvironment.init(isDebug)
    Logger.init(logToConsole = isDebug)
    CrashReporting.init()
    SingletonImageLoader.setSafe(AppImageLoaderFactory())
}
