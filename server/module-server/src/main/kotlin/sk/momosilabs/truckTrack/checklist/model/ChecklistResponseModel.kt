package sk.momosilabs.truckTrack.checklist.model

import java.time.OffsetDateTime
import java.util.UUID

data class ChecklistResponseModel(
    val id: Long = 0L,
    val truckId: Long,
    val trailerId: Long,
    val answeredById: UUID,
    val answeredAt: OffsetDateTime,
    val answers: ChecklistResponseAnswers,
)

data class ChecklistResponseAnswers(
    val truck: VehicleSnapshot,
    val trailer: VehicleSnapshot,
    val items: List<ChecklistResponseItem>,
)

data class VehicleSnapshot(
    val id: Long,
    val licensePlate: String,
)

data class ChecklistResponseItem(
    val identifier: String,
    val texts: List<ChecklistText>,
    val answer: Boolean,
)

data class ChecklistText(
    val language: String,
    val text: String,
)
