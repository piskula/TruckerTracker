package sk.momosilabs.truckTrack.push.service.registerPushDevice

import sk.momosilabs.truckTrack.push.entity.PushPlatform
import sk.momosilabs.truckTrack.push.model.PushDeviceModel

data class RegisterPushDeviceCommand(
    val firebaseInstallationId: String,
    val deviceName: String,
    val platform: PushPlatform,
)

interface RegisterPushDeviceUseCase {

    fun register(command: RegisterPushDeviceCommand): PushDeviceModel
}
