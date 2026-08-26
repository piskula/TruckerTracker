package com.momosi.trucktrack.shared.issue

import kotlinx.serialization.Serializable

@Serializable
enum class IssueStatusDto {
    OPEN, IN_PROGRESS, DONE, CANCELLED
}
