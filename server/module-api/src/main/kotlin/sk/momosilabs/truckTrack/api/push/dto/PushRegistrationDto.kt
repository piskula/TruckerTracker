package sk.momosilabs.truckTrack.api.push.dto

data class PushRegistrationDto(
    val installationId: String = "",
    val platform: PushPlatformDto = PushPlatformDto.UNKNOWN,
    val deviceLabel: String = "",
)
