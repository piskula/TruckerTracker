package sk.momosilabs.truckTrack.api.push.dto

data class PushDeviceRegistrationDto(
    val firebaseInstallationId: String,
    val deviceName: String,
    val platform: PushPlatformDto,
)
