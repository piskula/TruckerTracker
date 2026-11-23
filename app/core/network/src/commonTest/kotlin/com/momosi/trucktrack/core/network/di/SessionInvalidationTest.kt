package com.momosi.trucktrack.core.network.di

import com.momosi.trucktrack.core.common.coroutines.runCatchingCancellable
import com.momosi.trucktrack.core.common.network.ApiException
import com.momosi.trucktrack.user.model.AuthenticationState
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.get
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

class SessionInvalidationTest {

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
}
