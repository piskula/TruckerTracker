package com.momosi.trucktrack.core.push.registration.api

import com.momosi.trucktrack.shared.push.PushDeviceDto
import com.momosi.trucktrack.shared.push.PushDeviceRegistrationDto
import com.momosi.trucktrack.shared.push.PushDeviceUpdateDto
import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.DELETE
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.PATCH
import de.jensklingenberg.ktorfit.http.PUT
import de.jensklingenberg.ktorfit.http.Path
import de.jensklingenberg.ktorfit.http.ReqBuilder
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.statement.HttpResponse

internal interface PushDeviceApi {

    @PUT("api/v1/push-device")
    suspend fun register(@Body registration: PushDeviceRegistrationDto, @ReqBuilder requestOptions: HttpRequestBuilder.() -> Unit): PushDeviceDto

    @PATCH("api/v1/push-device/{firebaseInstallationId}")
    suspend fun update(@Path("firebaseInstallationId") lastSentFirebaseInstallationId: String, @Body pushDeviceUpdate: PushDeviceUpdateDto)

    @GET("api/v1/push-device")
    suspend fun current(): HttpResponse

    @DELETE("api/v1/push-device/{firebaseInstallationId}")
    suspend fun unregister(@Path("firebaseInstallationId") firebaseInstallationId: String)
}
