package com.momosi.trucktrack.core.push.message

import com.momosi.trucktrack.shared.push.PushMessageTypeDto

sealed class IncomingPushMessage(val type: PushMessageTypeDto?) {

    data class DeviceUnregistered(val newDeviceName: String?) : IncomingPushMessage(PushMessageTypeDto.DEVICE_UNREGISTERED)

    data class Unrecognized(val rawType: String?) : IncomingPushMessage(null)
}
