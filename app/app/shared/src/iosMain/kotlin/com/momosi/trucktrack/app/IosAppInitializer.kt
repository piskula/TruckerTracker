package com.momosi.trucktrack.app

import com.momosi.trucktrack.core.applinks.AppLinksManager
import com.momosi.trucktrack.core.common.config.AppConfig
import org.koin.mp.KoinPlatform

fun openIosAppLink(url: String): Boolean = KoinPlatform.getKoin().get<AppLinksManager>().openUrl(url)

fun bootstrapIosApp(
    isDebug: Boolean,
    apiBaseUrl: String,
    realmUrl: String,
    oauthClientId: String,
    appScheme: String,
) {
    initApp(isDebug = isDebug)
    initKoin(
        AppConfig(
            apiBaseUrl = apiBaseUrl,
            realmUrl = realmUrl,
            oauthClientId = oauthClientId,
            appScheme = appScheme,
        ),
    )
}
