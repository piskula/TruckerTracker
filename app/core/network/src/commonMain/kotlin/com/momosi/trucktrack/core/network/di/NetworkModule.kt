package com.momosi.trucktrack.core.network.di

import com.momosi.trucktrack.core.common.TruckTrackConfig
import com.momosi.trucktrack.core.common.logger.Logger
import com.momosi.trucktrack.core.common.network.installApiExceptionMapping
import com.momosi.trucktrack.core.common.network.isTransientNetworkFailure
import com.momosi.trucktrack.core.network.httpClientEngineFactory
import com.momosi.trucktrack.user.AuthManager
import com.momosi.trucktrack.user.model.TokenResponse
import de.jensklingenberg.ktorfit.Ktorfit
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.ResponseException
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.qualifier.named
import org.koin.dsl.module

val networkModule = module {
    single {
        Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }
    }

    single {
        val json: Json = get()
        val authManager: AuthManager = get()
        buildHttpClient(authManager) {
            defaultRequest {
                url(TruckTrackConfig.API_BASE_URL)
                contentType(ContentType.Application.Json)
            }
            install(ContentNegotiation) {
                json(json)
            }
        }
    }

    single {
        Ktorfit.Builder()
            .baseUrl(TruckTrackConfig.API_BASE_URL)
            .httpClient(get<HttpClient>())
            .build()
    }

    single(named("image")) {
        val authManager: AuthManager = get()
        buildHttpClient(authManager) {
            defaultRequest {
                url(TruckTrackConfig.API_BASE_URL)
            }
        }
    }
}

private fun buildHttpClient(authManager: AuthManager, block: HttpClientConfig<*>.() -> Unit = {}): HttpClient = HttpClient(httpClientEngineFactory()) {
    expectSuccess = true

    block()

    install(Auth) {
        bearer {
            loadTokens {
                authManager.token().toBearerTokens()
            }
            refreshTokens {
                authManager.token().toBearerTokens()
            }
        }
    }

    install(Logging) {
        level = LogLevel.BODY
        logger = object : io.ktor.client.plugins.logging.Logger {
            override fun log(message: String) {
                Logger.d("Network", message)
            }
        }
    }

    installApiExceptionMapping { cause, request ->
        val response = (cause as? ResponseException)?.response
        val durationMs = response?.let { it.responseTime.timestamp - it.requestTime.timestamp }

        Logger.d(
            "Network",
            "Request failed: ${request.method.value} ${request.url.encodedPath}" +
                (response?.let { " status=${it.status.value} durationMs=$durationMs" } ?: " (no response)"),
        )
    }
}

private fun TokenResponse.toBearerTokens(): BearerTokens? = when (this) {
    is TokenResponse.Token -> BearerTokens(accessToken = token, refreshToken = "")

    is TokenResponse.TokenError -> {
        if (exception.isTransientNetworkFailure()) {
            Logger.w("Network", exception, "Token refresh failed, sending request unauthenticated (offline)")
        } else {
            Logger.e("Network", exception, "Token refresh failed, sending request unauthenticated")
        }
        null
    }

    TokenResponse.GuestWithoutToken -> null
}
