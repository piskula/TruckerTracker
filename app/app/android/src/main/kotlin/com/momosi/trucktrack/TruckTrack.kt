package com.momosi.trucktrack

import android.app.Application
import com.momosi.trucktrack.app.initApp
import com.momosi.trucktrack.app.initKoin
import com.momosi.trucktrack.core.common.config.AppConfig
import org.koin.android.ext.koin.androidContext

class TruckTrack : Application() {

    override fun onCreate() {
        super.onCreate()
        initKoin(appConfig()) { androidContext(this@TruckTrack) }
        initApp(isDebug = BuildConfig.DEBUG)
    }

    private fun appConfig() = AppConfig(
        apiBaseUrl = BuildConfig.API_BASE_URL,
        realmUrl = BuildConfig.REALM_URL,
        oauthClientId = BuildConfig.OAUTH_CLIENT_ID,
        appScheme = BuildConfig.APP_SCHEME,
    )
}
