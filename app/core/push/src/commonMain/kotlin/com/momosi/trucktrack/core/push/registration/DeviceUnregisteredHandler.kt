package com.momosi.trucktrack.core.push.registration

import com.momosi.trucktrack.core.common.logger.Logger
import com.momosi.trucktrack.core.push.message.IncomingPushMessage
import com.momosi.trucktrack.core.push.message.PushMessageHandler
import com.momosi.trucktrack.shared.push.PushMessageTypeDto

private const val TAG = "PushRegistration"

internal class DeviceUnregisteredHandler(private val pushRegistrationManager: PushRegistrationManager) : PushMessageHandler {

    override val type: PushMessageTypeDto = PushMessageTypeDto.DEVICE_UNREGISTERED

    override suspend fun handle(message: IncomingPushMessage) {
        Logger.i(TAG, "Notifications moved to another device")
        pushRegistrationManager.refreshState()
    }
}
