package com.momosi.trucktrack.core.network.di

import com.momosi.trucktrack.core.common.coroutines.runCatchingCancellable
import com.momosi.trucktrack.core.common.language.AppLanguage
import com.momosi.trucktrack.core.common.language.LanguageRepository
import com.momosi.trucktrack.core.common.network.ApiException
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
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.get
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

class SessionInvalidationTest {

    @Test
    fun acceptLanguageTracksTheSelectedAppLanguageOnEveryRequest() = runTest {
        val languageRepository = FakeLanguageRepository()
        val requestLanguages = mutableListOf<List<String>?>()

        withClient(FakeAuthManager(), {
            requestLanguages += it.headers.getAll(HttpHeaders.AcceptLanguage)
            respond("[]", HttpStatusCode.OK)
        }, languageRepository) { client ->
            client.get("https://api.example.test/issues")
            languageRepository.setLanguage(AppLanguage.Slovak)
            client.get("https://api.example.test/issues")

            assertEquals(listOf<List<String>?>(listOf("en"), listOf("sk")), requestLanguages)
        }
    }

    @Test
    fun finalUnauthorizedResponseInvalidatesTheRetriedToken() = runTest {
        val authManager = FakeAuthManager(listOf("initial-token", "refreshed-token"))
        val requestTokens = mutableListOf<String?>()

        withClient(authManager, {
            requestTokens += it.headers[HttpHeaders.Authorization]
            respond("", HttpStatusCode.Unauthorized, headersOf(HttpHeaders.WWWAuthenticate, "Bearer"))
        }) { client ->
            val result = runCatchingCancellable { client.get("https://api.example.test/issues") }

            assertIs<ApiException.Unauthorized>(result.exceptionOrNull())
            assertEquals(listOf<String?>("Bearer initial-token", "Bearer refreshed-token"), requestTokens)
            assertEquals("refreshed-token", authManager.rejectedAccessToken)
            assertEquals(AuthenticationState.Guest, authManager.authenticationState.value)
        }
    }

    @Test
    fun successfulAuthenticationRetryKeepsTheSession() = runTest {
        val authManager = FakeAuthManager(listOf("initial-token", "refreshed-token"))
        var requests = 0

        withClient(authManager, {
            requests++
            if (requests == 1) {
                respond("", HttpStatusCode.Unauthorized, headersOf(HttpHeaders.WWWAuthenticate, "Bearer"))
            } else {
                respond("[]", HttpStatusCode.OK)
            }
        }) { client ->
            assertEquals(HttpStatusCode.OK, client.get("https://api.example.test/issues").status)
            assertEquals(2, requests)
            assertNull(authManager.rejectedAccessToken)
            assertEquals(AuthenticationState.Authorized, authManager.authenticationState.value)
        }
    }

    @Test
    fun forbiddenAndServerErrorsKeepTheSession() = runTest {
        for (status in listOf(HttpStatusCode.Forbidden, HttpStatusCode.InternalServerError)) {
            val authManager = FakeAuthManager()

            withClient(authManager, { respond("", status) }) { client ->
                val result = runCatchingCancellable { client.get("https://api.example.test/issues") }

                assertEquals(status.value, assertIs<ApiException.HttpError>(result.exceptionOrNull()).statusCode)
                assertNull(authManager.rejectedAccessToken)
                assertEquals(AuthenticationState.Authorized, authManager.authenticationState.value)
            }
        }
    }

    @Test
    fun connectivityFailureKeepsTheSession() = runTest {
        val authManager = FakeAuthManager()

        withClient(authManager, { throw ApiException.NoConnection }) { client ->
            val result = runCatchingCancellable { client.get("https://api.example.test/issues") }

            assertIs<ApiException.NoConnection>(result.exceptionOrNull())
            assertNull(authManager.rejectedAccessToken)
            assertEquals(AuthenticationState.Authorized, authManager.authenticationState.value)
        }
    }

    @Test
    fun unauthenticatedUnauthorizedResponseDoesNotInvalidateAnotherSession() = runTest {
        val authManager = FakeAuthManager(emptyList())

        withClient(authManager, { respond("", HttpStatusCode.Unauthorized) }) { client ->
            val result = runCatchingCancellable { client.get("https://api.example.test/issues") }

            assertIs<ApiException.Unauthorized>(result.exceptionOrNull())
            assertNull(authManager.rejectedAccessToken)
        }
    }

    private suspend fun TestScope.withClient(
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
}

private class FakeLanguageRepository : LanguageRepository {
    override val language = MutableStateFlow(AppLanguage.English)
    override val requiresRestartToApply = false

    override fun setLanguage(language: AppLanguage) {
        this.language.value = language
    }
}

private class FakeAuthManager(private val tokens: List<String> = listOf("access-token")) : AuthManager {
    override val authenticationState = MutableStateFlow(AuthenticationState.Authorized)
    override val authenticationInProgress = MutableStateFlow(false)
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

    override suspend fun signOut(): Unit = error("Unauthorized responses must not launch browser logout")
}
