package com.momosi.trucktrack.core.common.version.api

import com.momosi.trucktrack.core.common.TruckTrackConfig
import com.momosi.trucktrack.core.common.network.installApiExceptionMapping
import com.momosi.trucktrack.shared.version.BuildInfoDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class VersionApi {

    private val client by lazy {
        HttpClient {
            expectSuccess = true
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
            installApiExceptionMapping()
        }
    }

    suspend fun getBuildInfo(): BuildInfoDto = client.get("${TruckTrackConfig.API_BASE_URL}api/v1/version").body()
}
