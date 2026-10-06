package com.momosi.trucktrack.core.common.config

data class AppConfig(val apiBaseUrl: String, val realmUrl: String, val oauthClientId: String, val appScheme: String) {
    init {
        requireBaseUrl("apiBaseUrl", apiBaseUrl)
        requireBaseUrl("realmUrl", realmUrl)
        require(oauthClientId.isNotBlank()) { "oauthClientId must not be blank" }
        require(appScheme.isNotBlank()) { "appScheme must not be blank" }
    }

    val oauthRedirectUrl: String get() = "$appScheme://auth/callback"
    val oauthLogoutRedirectUrl: String get() = "$appScheme://auth/logout"
}

private fun requireBaseUrl(name: String, value: String) {
    require(value.startsWith("https://") && value.endsWith("/")) {
        "$name must be an https:// URL ending with '/', was '$value'"
    }
}
