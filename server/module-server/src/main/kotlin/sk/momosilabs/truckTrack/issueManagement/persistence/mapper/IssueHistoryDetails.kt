package sk.momosilabs.truckTrack.issueManagement.persistence.mapper

import sk.momosilabs.truckTrack.account.model.AccountModel
import sk.momosilabs.truckTrack.issueManagement.entity.IssuePriority
import sk.momosilabs.truckTrack.issueManagement.model.IssueHistoryModel

data class CommentDetails(
    val commentText: String,
)

data class TitleChangeDetails(
    val titleFrom: String,
    val titleTo: String,
)

data class DescriptionChangeDetails(
    val descriptionFrom: String,
    val descriptionTo: String,
)

data class AssigneeChangeDetails(
    val assigneeFrom: AccountModel?,
    val assigneeTo: AccountModel?,
)

data class PriorityChangeDetails(
    val priorityFrom: IssuePriority,
    val priorityTo: IssuePriority,
)

data class VehicleChangeDetails(
    val vehicleFrom: IssueHistoryModel.VehicleSnapshot,
    val vehicleTo: IssueHistoryModel.VehicleSnapshot,
)
