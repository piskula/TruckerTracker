package com.momosi.trucktrack.shared.checklist

import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlinx.serialization.Serializable

@OptIn(ExperimentalUuidApi::class)
@Serializable
data class ChecklistResponseDto(
    val id: Long,
    val truck: ChecklistVehicleSnapshotDto,
    val trailer: ChecklistVehicleSnapshotDto,
    val answeredBy: Uuid,
    val answeredAt: Instant,
    val items: List<ChecklistResponseItemDto>,
)
