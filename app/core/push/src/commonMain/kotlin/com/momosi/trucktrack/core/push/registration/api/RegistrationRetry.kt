package com.momosi.trucktrack.core.push.registration.api

import com.momosi.trucktrack.core.common.network.isTransientNetworkFailure
import io.ktor.client.plugins.retry
import io.ktor.client.request.HttpRequestBuilder
import kotlinx.coroutines.CancellationException

private const val REGISTRATION_MAX_RETRIES = 3
private const val REGISTRATION_RETRY_BASE_DELAY_MS = 500L
private const val REGISTRATION_RETRY_MAX_DELAY_MS = 2_000L
private const val REGISTRATION_RETRY_RANDOMIZATION_MS = 250L
private val SERVER_ERRORS = 500..599

internal fun HttpRequestBuilder.registrationRetry() = retry {
    retryIf(REGISTRATION_MAX_RETRIES) { _, response -> response.status.value in SERVER_ERRORS }
    retryOnExceptionIf(REGISTRATION_MAX_RETRIES) { _, cause -> cause !is CancellationException && cause.isTransientNetworkFailure() }
    exponentialDelay(
        baseDelayMs = REGISTRATION_RETRY_BASE_DELAY_MS,
        maxDelayMs = REGISTRATION_RETRY_MAX_DELAY_MS,
        randomizationMs = REGISTRATION_RETRY_RANDOMIZATION_MS,
        respectRetryAfterHeader = false,
    )
}
