package com.momosi.trucktrack.core.network.di

import com.momosi.trucktrack.core.common.language.AppLanguage
import com.momosi.trucktrack.core.common.language.LanguageRepository
import com.momosi.trucktrack.user.AuthManager
import com.momosi.trucktrack.user.model.AuthActionResult
import com.momosi.trucktrack.user.model.AuthenticationState
import com.momosi.trucktrack.user.model.TokenResponse
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockEngineConfig
import io.ktor.client.engine.mock.MockRequestHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.TestScope

internal suspend fun TestScope.withClient(
    authManager: AuthManager,
    handler: MockRequestHandler,
    languageRepository: LanguageRepository = FakeLanguageRepository(),
    block: suspend (HttpClient) -> Unit,
) {
    val factory = object : HttpClientEngineFactory<MockEngineConfig> {
        override fun create(block: MockEngineConfig.() -> Unit): HttpClientEngine = MockEngine.create {
            block()
            addHandler(handler)
        }
    }
    val client = buildHttpClient(authManager, backgroundScope, languageRepository, factory)
    try {
        block(client)
    } finally {
        client.close()
    }
}

internal class FakeLanguageRepository : LanguageRepository {
    override val language = MutableStateFlow(AppLanguage.English)
    override val requiresRestartToApply = false

    override fun setLanguage(language: AppLanguage) {
        this.language.value = language
    }
}

internal class FakeAuthManager(private val tokens: List<String> = listOf("access-token")) : AuthManager {
    override val authenticationState = MutableStateFlow(AuthenticationState.Authorized)
    override val authenticationInProgress = MutableStateFlow(false)
    override val loginCompletions = emptyFlow<Unit>()
    var rejectedAccessToken: String? = null
        private set
    private var tokenRequests = 0

    override suspend fun token(): TokenResponse {
        if (tokens.isEmpty()) return TokenResponse.GuestWithoutToken
        val token = tokens[minOf(tokenRequests, tokens.lastIndex)]
        tokenRequests++
        return TokenResponse.Token(token)
    }

    override suspend fun invalidateSession(rejectedAccessToken: String) {
        this.rejectedAccessToken = rejectedAccessToken
        authenticationState.value = AuthenticationState.Guest
    }

    override suspend fun signIn(): AuthActionResult = error("Unexpected sign-in")

    override suspend fun signOut(): AuthActionResult = error("Unauthorized responses must not launch browser logout")
}
