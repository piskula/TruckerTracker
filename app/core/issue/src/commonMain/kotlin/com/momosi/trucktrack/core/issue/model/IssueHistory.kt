package com.momosi.trucktrack.core.issue.model

import kotlin.time.Instant

sealed interface IssueHistory {
    val id: String
    val performedBy: Account?
    val createdAt: Instant

    data class StatusChange(override val id: String, override val performedBy: Account?, override val createdAt: Instant, val statusFrom: IssueStatus?, val statusTo: IssueStatus) : IssueHistory

    data class AssigneeChange(override val id: String, override val performedBy: Account?, override val createdAt: Instant) : IssueHistory

    data class Comment(override val id: String, override val performedBy: Account?, override val createdAt: Instant, val commentText: String) : IssueHistory

    data class Update(override val id: String, override val performedBy: Account?, override val createdAt: Instant, val changedFields: List<IssueUpdatedField>) : IssueHistory
}
