package com.momosi.trucktrack.shared.issue

import kotlinx.serialization.Serializable

@Serializable
data class IssueUpdateDto(
    val vehicleId: Long,
    val title: String,
    val description: String,
    val priority: IssuePriorityDto,
)
