package sk.momosilabs.truckTrack.issueManagement.service.updateIssue

import sk.momosilabs.truckTrack.issueManagement.entity.IssuePriority
import sk.momosilabs.truckTrack.issueManagement.model.IssueModel

data class UpdateIssueCommand(
    val vehicleId: Long,
    val title: String,
    val description: String,
    val priority: IssuePriority,
)

interface UpdateIssueUseCase {

    fun update(issueId: Long, command: UpdateIssueCommand): IssueModel
}
