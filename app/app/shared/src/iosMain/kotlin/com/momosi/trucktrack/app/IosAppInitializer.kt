package com.momosi.trucktrack.app

import com.momosi.trucktrack.core.common.config.AppConfig

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
