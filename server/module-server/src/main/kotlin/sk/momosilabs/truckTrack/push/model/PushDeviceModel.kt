package sk.momosilabs.truckTrack.push.model

import sk.momosilabs.truckTrack.push.entity.PushPlatform
import java.time.OffsetDateTime
import java.util.UUID

data class PushDeviceModel(
    val id: Long,
    val accountId: UUID,
    val firebaseInstallationId: String,
    val deviceName: String,
    val platform: PushPlatform,
    val registeredAt: OffsetDateTime,
    val updatedAt: OffsetDateTime,
    val sessionId: String? = null,
)
