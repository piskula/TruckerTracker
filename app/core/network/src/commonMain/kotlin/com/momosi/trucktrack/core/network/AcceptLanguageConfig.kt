package com.momosi.trucktrack.core.network

internal class AcceptLanguageConfig {
    internal var languageProvider: (() -> String)? = null
        private set

    fun language(provider: () -> String) {
        languageProvider = provider
    }
}
