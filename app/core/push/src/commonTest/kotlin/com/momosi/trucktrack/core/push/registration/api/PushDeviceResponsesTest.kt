package com.momosi.trucktrack.core.push.registration.api

import com.momosi.trucktrack.core.common.network.ApiException
import com.momosi.trucktrack.core.push.pushDeviceApiRespondingWith
import com.momosi.trucktrack.shared.push.PushDeviceUpdateDto
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

private const val LAST_SENT_FIREBASE_INSTALLATION_ID = "cZ2bN3k4Rz-Qp8_wX1yT7m"

class PushDeviceResponsesTest {

    @Test
    fun noContentMeansNoDeviceIsRegistered() = runTest {
        val api = pushDeviceApiRespondingWith { respond("", HttpStatusCode.NoContent) }

        assertNull(api.currentPushDevice())
    }

    @Test
    fun failedLookupIsReportedInsteadOfNoDevice() = runTest {
        val api = pushDeviceApiRespondingWith { respond("", HttpStatusCode.ServiceUnavailable) }

        val failure = assertFailsWith<ApiException.HttpError> { api.currentPushDevice() }

        assertEquals(HttpStatusCode.ServiceUnavailable.value, failure.statusCode)
    }

    @Test
    fun conflictMeansThisDeviceNoLongerHoldsTheSlot() = runTest {
        var requestedPath: String? = null
        var requestedMethod: HttpMethod? = null
        val api = pushDeviceApiRespondingWith { request ->
            requestedPath = request.url.encodedPath
            requestedMethod = request.method
            respond("", HttpStatusCode.Conflict)
        }

        val result = api.updatePushDevice(
            LAST_SENT_FIREBASE_INSTALLATION_ID,
            PushDeviceUpdateDto(firebaseInstallationId = "dK9fL2m5Sa-Vr3_uY6zP0n"),
        )

        assertEquals(PushDeviceUpdateResult.NotSlotHolder, result)
        assertEquals(HttpMethod.Patch, requestedMethod)
        assertEquals("/api/v1/push-device/$LAST_SENT_FIREBASE_INSTALLATION_ID", requestedPath)
    }
}
