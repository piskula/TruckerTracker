package com.momosi.trucktrack.core.push.registration

import com.momosi.trucktrack.core.push.PushTestEnvironment
import com.momosi.trucktrack.core.push.THIS_DEVICE_FIREBASE_INSTALLATION_ID
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class UnregisterPushDeviceOnSignOutTest {

    @Test
    fun unregistersThisDeviceBeforeTheSessionIsCleared() = runTest {
        val environment = PushTestEnvironment(this)
        environment.manager.registerThisDevice()

        UnregisterPushDeviceOnSignOut(environment.manager).beforeSessionCleared()

        assertEquals(listOf(THIS_DEVICE_FIREBASE_INSTALLATION_ID), environment.server.unregistrations)
        assertEquals(PushDeviceState.None, environment.manager.state.value)
    }
}
