package com.momosi.trucktrack.user.internal.api

import com.momosi.trucktrack.core.common.config.AppConfig
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class AuthApi(private val appConfig: AppConfig) {

    private val client by lazy {
        HttpClient {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
    }

    suspend fun getRealmInfo(): RealmDto = client.get(appConfig.realmUrl).body()

    suspend fun logout(idToken: String) {
        client.post("${appConfig.realmUrl}protocol/openid-connect/logout") {
            contentType(ContentType.Application.FormUrlEncoded)
            setBody("id_token_hint=$idToken")
        }
    }
}
