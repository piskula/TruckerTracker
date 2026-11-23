package sk.momosilabs.truckTrack.push.model

import sk.momosilabs.truckTrack.push.entity.PushPlatform
import java.time.OffsetDateTime
import java.util.UUID

data class PushRegistrationModel(
    val accountId: UUID,
    val installationId: String,
    val platform: PushPlatform,
    val deviceLabel: String,
    val registeredAt: OffsetDateTime,
)
