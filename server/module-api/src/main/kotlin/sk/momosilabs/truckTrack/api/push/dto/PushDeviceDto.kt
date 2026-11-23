package sk.momosilabs.truckTrack.api.push.dto

import java.time.OffsetDateTime

data class PushDeviceDto(
    val firebaseInstallationId: String,
    val deviceName: String,
    val platform: PushPlatformDto,
    val registeredAt: OffsetDateTime,
)
