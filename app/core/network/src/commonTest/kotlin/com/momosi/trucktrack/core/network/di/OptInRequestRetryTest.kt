package com.momosi.trucktrack.core.network.di

import com.momosi.trucktrack.core.common.coroutines.runCatchingCancellable
import com.momosi.trucktrack.core.common.network.ApiException
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.retry
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.put
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.test.runTest
import kotlinx.io.IOException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

private const val URL = "https://api.example.test/push-device"
private const val OPTED_IN_RETRIES = 2

class OptInRequestRetryTest {

    @Test
    fun requestsAreNotRetriedUnlessTheyOptIn() = runTest {
        var requests = 0

        withClient(FakeAuthManager(), {
            requests++
            respond("", HttpStatusCode.ServiceUnavailable)
        }) { client ->
            val failure = runCatchingCancellable { client.get(URL) }.exceptionOrNull()

            assertEquals(HttpStatusCode.ServiceUnavailable.value, assertIs<ApiException.HttpError>(failure).statusCode)
            assertEquals(1, requests)
        }
    }

    @Test
    fun optedInRequestIsRetriedAfterServerAndTransportErrors() = runTest {
        var requests = 0

        withClient(FakeAuthManager(), {
            requests++
            when (requests) {
                1 -> respond("", HttpStatusCode.ServiceUnavailable)
                2 -> throw IOException("Connection reset")
                else -> respond("", HttpStatusCode.OK)
            }
        }) { client ->
            assertEquals(HttpStatusCode.OK, client.put(URL) { optInToRetries() }.status)
            assertEquals(3, requests)
        }
    }

    @Test
    fun optedInRequestIsNotRetriedOnClientErrors() = runTest {
        var requests = 0

        withClient(FakeAuthManager(), {
            requests++
            respond("", HttpStatusCode.Conflict)
        }) { client ->
            val failure = runCatchingCancellable { client.put(URL) { optInToRetries() } }.exceptionOrNull()

            assertEquals(HttpStatusCode.Conflict.value, assertIs<ApiException.HttpError>(failure).statusCode)
            assertEquals(1, requests)
        }
    }

    @Test
    fun finalFailureOfAnOptedInRequestIsMappedToAnApiException() = runTest {
        var requests = 0

        withClient(FakeAuthManager(), {
            requests++
            respond("", HttpStatusCode.ServiceUnavailable)
        }) { client ->
            val failure = runCatchingCancellable { client.put(URL) { optInToRetries() } }.exceptionOrNull()

            assertEquals(HttpStatusCode.ServiceUnavailable.value, assertIs<ApiException.HttpError>(failure).statusCode)
            assertEquals(1 + OPTED_IN_RETRIES, requests)
        }
    }

    private fun HttpRequestBuilder.optInToRetries() = retry {
        retryOnExceptionOrServerErrors(OPTED_IN_RETRIES)
        constantDelay(millis = 1, randomizationMs = 0)
    }
}
