package sk.momosilabs.truckTrack.push.service.updateRegisteredDevice

import sk.momosilabs.truckTrack.push.entity.PushPlatform

interface UpdateRegisteredDeviceUseCase {

    fun update(command: UpdateRegisteredDeviceCommand)
}

data class UpdateRegisteredDeviceCommand(
    val installationId: String,
    val platform: PushPlatform,
    val deviceLabel: String,
)
