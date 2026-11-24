package com.momosi.trucktrack.core.network

import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.http.HttpHeaders

internal val AcceptLanguage = createClientPlugin("AcceptLanguage", ::AcceptLanguageConfig) {
    val languageProvider = requireNotNull(pluginConfig.languageProvider) {
        "AcceptLanguage requires a language provider."
    }

    onRequest { request, _ ->
        request.headers[HttpHeaders.AcceptLanguage] = languageProvider()
    }
}
