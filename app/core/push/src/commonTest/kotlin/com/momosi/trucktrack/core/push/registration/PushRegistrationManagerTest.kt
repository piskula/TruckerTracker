package com.momosi.trucktrack.core.push.registration

import com.momosi.trucktrack.core.common.network.ApiException
import com.momosi.trucktrack.core.push.PushTestEnvironment
import com.momosi.trucktrack.core.push.THIS_DEVICE_FIREBASE_INSTALLATION_ID
import com.momosi.trucktrack.shared.push.PushDeviceUpdateDto
import com.momosi.trucktrack.user.model.AuthenticationState
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.time.Duration.Companion.seconds

private const val ROTATED_FIREBASE_INSTALLATION_ID = "firebase-installation-this-phone-rotated"

class PushRegistrationManagerTest {

    @Test
    fun loginCompletedBeforeTheManagerStartsRegistersExactlyOnce() = runTest {
        val environment = PushTestEnvironment(this)
        environment.authManager.completeLogin()

        val manager = environment.startManager()
        runCurrent()

        assertEquals(1, environment.server.registrations.size)
        assertEquals(PushDeviceState.ThisDevice, manager.state.value)

        environment.authManager.completeLogin()
        runCurrent()

        assertEquals(2, environment.server.registrations.size)
    }

    @Test
    fun firebaseInstallationIdChangeIsNotSentBeforeThisDeviceRegistered() = runTest {
        val environment = PushTestEnvironment(this)
        environment.startManager()
        runCurrent()

        rotateFirebaseInstallationId(environment)

        assertEquals(emptyList(), environment.server.requests)
    }

    @Test
    fun firebaseInstallationIdChangeIsSentToTheLastSentFirebaseInstallationId() = runTest {
        val environment = PushTestEnvironment(this)
        environment.storage.lastSentFirebaseInstallationId = THIS_DEVICE_FIREBASE_INSTALLATION_ID
        environment.startManager()
        runCurrent()
        assertEquals(emptyList(), environment.server.requests)

        rotateFirebaseInstallationId(environment)
        rotateFirebaseInstallationId(environment)

        assertEquals(
            listOf(THIS_DEVICE_FIREBASE_INSTALLATION_ID to PushDeviceUpdateDto(firebaseInstallationId = ROTATED_FIREBASE_INSTALLATION_ID)),
            environment.server.updates,
        )
        assertEquals(ROTATED_FIREBASE_INSTALLATION_ID, environment.storage.lastSentFirebaseInstallationId)
        assertEquals(emptyList(), environment.server.registrations)
    }

    @Test
    fun conflictRemembersTheSentFirebaseInstallationIdAndNeverRegisters() = runTest {
        val environment = PushTestEnvironment(this)
        environment.storage.lastSentFirebaseInstallationId = THIS_DEVICE_FIREBASE_INSTALLATION_ID
        environment.server.rejectsUpdates = true
        environment.startManager()
        runCurrent()

        rotateFirebaseInstallationId(environment)
        rotateFirebaseInstallationId(environment)

        assertEquals(1, environment.server.updates.size)
        assertEquals(ROTATED_FIREBASE_INSTALLATION_ID, environment.storage.lastSentFirebaseInstallationId)
        assertEquals(emptyList(), environment.server.registrations)
    }

    @Test
    fun registrationFailingAfterItsRetriesIsReported() = runTest {
        val environment = PushTestEnvironment(this)
        environment.server.unavailableResponses = Int.MAX_VALUE
        environment.authManager.completeLogin()

        val manager = environment.startManager()
        advanceTimeBy(30.seconds)
        runCurrent()

        assertEquals(List(4) { HttpMethod.Put }, environment.server.requests)
        assertEquals(HttpStatusCode.ServiceUnavailable.value, assertIs<ApiException.HttpError>(environment.errorReporter.reported.single()).statusCode)
        assertEquals(PushDeviceState.Unknown, manager.state.value)
        assertNull(environment.storage.lastSentFirebaseInstallationId)
    }

    @Test
    fun onlyTheRegistrationIsRetried() = runTest {
        val environment = PushTestEnvironment(this)
        val manager = environment.startManager()

        environment.server.unavailableResponses = 1
        manager.registerThisDevice()
        environment.server.unavailableResponses = 1
        manager.refreshState()
        environment.server.unavailableResponses = 1
        rotateFirebaseInstallationId(environment)
        environment.server.unavailableResponses = 1
        manager.unregisterThisDevice()

        assertEquals(
            listOf(HttpMethod.Put, HttpMethod.Put, HttpMethod.Get, HttpMethod.Patch, HttpMethod.Delete),
            environment.server.requests,
        )
    }

    @Test
    fun signingOutResetsTheState() = runTest {
        val environment = PushTestEnvironment(this)
        val manager = environment.startManager()
        manager.registerThisDevice()
        assertEquals(PushDeviceState.ThisDevice, manager.state.value)

        environment.authManager.authenticationState.value = AuthenticationState.Guest
        runCurrent()

        assertEquals(PushDeviceState.Unknown, manager.state.value)
    }

    private suspend fun TestScope.rotateFirebaseInstallationId(environment: PushTestEnvironment) {
        environment.messagingClient.firebaseInstallationId = ROTATED_FIREBASE_INSTALLATION_ID
        environment.messagingClient.firebaseInstallationIdChanges.emit(ROTATED_FIREBASE_INSTALLATION_ID)
        runCurrent()
    }
}
