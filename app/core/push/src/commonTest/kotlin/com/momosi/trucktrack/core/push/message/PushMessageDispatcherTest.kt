package com.momosi.trucktrack.core.push.message

import com.momosi.trucktrack.core.push.OTHER_DEVICE_NAME
import com.momosi.trucktrack.core.push.PushTestEnvironment
import com.momosi.trucktrack.core.push.registration.DeviceUnregisteredHandler
import com.momosi.trucktrack.core.push.registration.PushDeviceState
import com.momosi.trucktrack.shared.push.PushMessageTypeDto
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.minutes

private val DEVICE_UNREGISTERED_DATA = mapOf("type" to "DEVICE_UNREGISTERED", "deviceName" to OTHER_DEVICE_NAME)

class PushMessageDispatcherTest {

    @Test
    fun parsedMessagesReachTheirHandlersAndSubscribers() = runTest {
        val environment = PushTestEnvironment(this)
        val handled = mutableListOf<IncomingPushMessage>()
        val handler = object : PushMessageHandler {
            override val type = PushMessageTypeDto.DEVICE_UNREGISTERED

            override suspend fun handle(message: IncomingPushMessage) {
                handled += message
            }
        }
        val pushMessages = environment.startMessageDispatcher(listOf(handler))
        val delivered = mutableListOf<IncomingPushMessage>()
        backgroundScope.launch { pushMessages.messages.toList(delivered) }
        runCurrent()

        environment.messagingClient.messages.emit(DEVICE_UNREGISTERED_DATA)
        environment.messagingClient.messages.emit(mapOf("type" to "SOMETHING_NEW"))
        runCurrent()

        assertEquals(listOf<IncomingPushMessage>(IncomingPushMessage.DeviceUnregistered(OTHER_DEVICE_NAME)), handled)
        assertEquals(
            listOf(IncomingPushMessage.DeviceUnregistered(OTHER_DEVICE_NAME), IncomingPushMessage.Unrecognized("SOMETHING_NEW")),
            delivered,
        )
    }

    @Test
    fun deviceUnregisteredRefreshesTheStateAndNeverRegisters() = runTest {
        val environment = PushTestEnvironment(this)
        environment.manager.registerThisDevice()
        environment.startMessageDispatcher(listOf(DeviceUnregisteredHandler(environment.manager)))
        runCurrent()
        environment.server.registeredDevice = environment.otherDeviceRegisteredAt(environment.clock.now + 1.minutes)

        environment.messagingClient.messages.emit(DEVICE_UNREGISTERED_DATA)
        runCurrent()

        assertEquals(PushDeviceState.OtherDevice(OTHER_DEVICE_NAME), environment.manager.state.value)
        assertEquals(1, environment.server.registrations.size)
    }
}
