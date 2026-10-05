package com.momosi.trucktrack.core.issue.model

import kotlin.time.Instant

sealed interface IssueHistory {
    val id: String
    val performedBy: Account?
    val createdAt: Instant

    data class StatusChange(override val id: String, override val performedBy: Account?, override val createdAt: Instant, val statusFrom: IssueStatus?, val statusTo: IssueStatus) : IssueHistory

    data class AssigneeChange(override val id: String, override val performedBy: Account?, override val createdAt: Instant, val assigneeTo: Account?) : IssueHistory

    data class Comment(override val id: String, override val performedBy: Account?, override val createdAt: Instant, val commentText: String) : IssueHistory

    data class TitleChange(override val id: String, override val performedBy: Account?, override val createdAt: Instant, val titleFrom: String, val titleTo: String) : IssueHistory

    data class DescriptionChange(override val id: String, override val performedBy: Account?, override val createdAt: Instant, val descriptionFrom: String, val descriptionTo: String) : IssueHistory

    data class PriorityChange(override val id: String, override val performedBy: Account?, override val createdAt: Instant, val priorityFrom: IssuePriority, val priorityTo: IssuePriority) : IssueHistory

    data class VehicleChange(override val id: String, override val performedBy: Account?, override val createdAt: Instant, val vehicleFromLicensePlate: String, val vehicleToLicensePlate: String) : IssueHistory
}
