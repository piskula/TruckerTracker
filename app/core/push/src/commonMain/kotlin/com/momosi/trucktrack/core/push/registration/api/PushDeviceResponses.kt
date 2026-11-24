package com.momosi.trucktrack.core.push.registration.api

import com.momosi.trucktrack.core.common.network.ApiException
import com.momosi.trucktrack.shared.push.PushDeviceDto
import com.momosi.trucktrack.shared.push.PushDeviceUpdateDto
import io.ktor.client.call.body
import io.ktor.http.HttpStatusCode

internal suspend fun PushDeviceApi.currentPushDevice(): PushDeviceDto? {
    val response = current()
    return if (response.status == HttpStatusCode.NoContent) null else response.body()
}

internal suspend fun PushDeviceApi.updatePushDevice(lastSentFirebaseInstallationId: String, pushDeviceUpdate: PushDeviceUpdateDto): PushDeviceUpdateResult = try {
    update(lastSentFirebaseInstallationId, pushDeviceUpdate)
    PushDeviceUpdateResult.Updated
} catch (exception: ApiException.HttpError) {
    if (exception.statusCode != HttpStatusCode.Conflict.value) throw exception
    PushDeviceUpdateResult.NotSlotHolder
}
