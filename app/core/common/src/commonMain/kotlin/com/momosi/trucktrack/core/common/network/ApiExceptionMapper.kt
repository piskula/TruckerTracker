package com.momosi.trucktrack.core.common.network

import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.HttpCallValidator
import io.ktor.client.plugins.ResponseException
import io.ktor.client.request.HttpRequest
import io.ktor.http.HttpStatusCode

fun Throwable.toApiException(): ApiException? = when {
    this is ApiException -> this
    isTransientNetworkFailure() -> ApiException.NoConnection
    this is ResponseException && response.status == HttpStatusCode.Unauthorized -> ApiException.Unauthorized
    this is ResponseException -> ApiException.HttpError
    else -> null
}

fun HttpClientConfig<*>.installApiExceptionMapping(onFailure: (cause: Throwable, request: HttpRequest) -> Unit = { _, _ -> }) {
    install(HttpCallValidator) {
        handleResponseExceptionWithRequest { cause, request ->
            onFailure(cause, request)
            throw cause.toApiException() ?: cause
        }
    }
}
