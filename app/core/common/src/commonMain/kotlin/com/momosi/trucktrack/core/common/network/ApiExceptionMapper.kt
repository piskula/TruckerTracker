package com.momosi.trucktrack.core.common.network

import com.momosi.trucktrack.core.common.coroutines.runCatchingCancellable
import com.momosi.trucktrack.shared.common.ErrorDto
import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.HttpCallValidator
import io.ktor.client.plugins.ResponseException
import io.ktor.client.request.HttpRequest
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import kotlinx.serialization.json.Json

private val errorJson = Json { ignoreUnknownKeys = true }

suspend fun Throwable.toApiException(): ApiException? = when {
    this is ApiException -> this

    isTransientNetworkFailure() -> ApiException.NoConnection

    this is ResponseException -> when (response.status) {
        HttpStatusCode.Unauthorized -> ApiException.Unauthorized
        else -> ApiException.HttpError(response.status.value, response.readUserMessage())
    }

    else -> null
}

private suspend fun HttpResponse.readUserMessage(): String? = runCatchingCancellable {
    errorJson.decodeFromString<ErrorDto>(bodyAsText()).userMessage
}.getOrNull()?.takeIf { it.isNotBlank() }

fun HttpClientConfig<*>.installApiExceptionMapping(onFailure: (cause: Throwable, request: HttpRequest) -> Unit = { _, _ -> }) {
    install(HttpCallValidator) {
        handleResponseExceptionWithRequest { cause, request ->
            onFailure(cause, request)
            throw cause.toApiException() ?: cause
        }
    }
}
