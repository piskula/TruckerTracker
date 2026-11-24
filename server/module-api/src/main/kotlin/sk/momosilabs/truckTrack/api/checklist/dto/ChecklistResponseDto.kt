package sk.momosilabs.truckTrack.api.checklist.dto

import java.time.OffsetDateTime
import java.util.UUID

data class ChecklistResponseDto(
    val id: Long,
    val truck: ChecklistVehicleSnapshotDto,
    val trailer: ChecklistVehicleSnapshotDto,
    val answeredBy: UUID,
    val answeredAt: OffsetDateTime,
    val items: List<ChecklistResponseItemDto>,
)
