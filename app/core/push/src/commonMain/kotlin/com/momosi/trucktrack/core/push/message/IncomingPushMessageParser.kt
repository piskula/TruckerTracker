package com.momosi.trucktrack.core.push.message

import com.momosi.trucktrack.shared.push.PushMessageTypeDto

private const val TYPE_KEY = "type"
private const val DEVICE_NAME_KEY = "deviceName"

internal fun Map<String, String>.toIncomingPushMessage(): IncomingPushMessage {
    val type = this[TYPE_KEY]
    return when (type) {
        PushMessageTypeDto.DEVICE_UNREGISTERED.name -> IncomingPushMessage.DeviceUnregistered(newDeviceName = this[DEVICE_NAME_KEY])
        else -> IncomingPushMessage.Unrecognized(rawType = type)
    }
}
