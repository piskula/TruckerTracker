package com.momosi.trucktrack.shared.issue

import kotlinx.serialization.Serializable

@Serializable
enum class IssueUpdatedFieldDto {
    TITLE,
    DESCRIPTION,
    PRIORITY,
    VEHICLE,
}
