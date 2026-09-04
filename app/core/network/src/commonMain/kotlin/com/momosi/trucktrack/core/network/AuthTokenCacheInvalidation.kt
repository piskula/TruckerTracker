package com.momosi.trucktrack.core.network

import com.momosi.trucktrack.user.AuthManager
import io.ktor.client.HttpClient
import io.ktor.client.plugins.auth.clearAuthTokens
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

fun HttpClient.invalidateAuthTokensOn(authManager: AuthManager, scope: CoroutineScope) {
    authManager.authenticationState
        .onEach { clearAuthTokens() }
        .launchIn(scope)
}
