package sk.momosilabs.truckTrack.issueManagement.persistence.mapper

import sk.momosilabs.truckTrack.account.persistence.mapper.toModel
import sk.momosilabs.truckTrack.issueManagement.entity.IssueHistoryEntity
import sk.momosilabs.truckTrack.issueManagement.entity.IssueHistoryEventType
import sk.momosilabs.truckTrack.issueManagement.model.IssueHistoryModel
import sk.momosilabs.truckTrack.util.toUtcOffsetDateTime
import tools.jackson.databind.ObjectMapper

fun IssueHistoryEntity.toModel(objectMapper: ObjectMapper): IssueHistoryModel = when (type) {
    IssueHistoryEventType.STATUS_CHANGE -> toStatusChange()
    IssueHistoryEventType.ASSIGNEE_CHANGE -> toAssigneeChange(objectMapper)
    IssueHistoryEventType.COMMENT -> toComment(objectMapper)
    IssueHistoryEventType.TITLE_CHANGE -> toTitleChange(objectMapper)
    IssueHistoryEventType.DESCRIPTION_CHANGE -> toDescriptionChange(objectMapper)
    IssueHistoryEventType.PRIORITY_CHANGE -> toPriorityChange(objectMapper)
    IssueHistoryEventType.VEHICLE_CHANGE -> toVehicleChange(objectMapper)
}

fun IssueHistoryModel.toDetailsJson(objectMapper: ObjectMapper): String? = when (this) {
    is IssueHistoryModel.StatusChange -> null
    is IssueHistoryModel.AssigneeChange -> objectMapper.writeValueAsString(AssigneeChangeDetails(assigneeFrom, assigneeTo))
    is IssueHistoryModel.Comment -> objectMapper.writeValueAsString(CommentDetails(commentText))
    is IssueHistoryModel.TitleChange -> objectMapper.writeValueAsString(TitleChangeDetails(titleFrom, titleTo))
    is IssueHistoryModel.DescriptionChange -> objectMapper.writeValueAsString(DescriptionChangeDetails(descriptionFrom, descriptionTo))
    is IssueHistoryModel.PriorityChange -> objectMapper.writeValueAsString(PriorityChangeDetails(priorityFrom, priorityTo))
    is IssueHistoryModel.VehicleChange -> objectMapper.writeValueAsString(VehicleChangeDetails(vehicleFrom, vehicleTo))
}

fun IssueHistoryModel.toEventType(): IssueHistoryEventType = when (this) {
    is IssueHistoryModel.StatusChange -> IssueHistoryEventType.STATUS_CHANGE
    is IssueHistoryModel.AssigneeChange -> IssueHistoryEventType.ASSIGNEE_CHANGE
    is IssueHistoryModel.Comment -> IssueHistoryEventType.COMMENT
    is IssueHistoryModel.TitleChange -> IssueHistoryEventType.TITLE_CHANGE
    is IssueHistoryModel.DescriptionChange -> IssueHistoryEventType.DESCRIPTION_CHANGE
    is IssueHistoryModel.PriorityChange -> IssueHistoryEventType.PRIORITY_CHANGE
    is IssueHistoryModel.VehicleChange -> IssueHistoryEventType.VEHICLE_CHANGE
}

private fun IssueHistoryEntity.toStatusChange() = IssueHistoryModel.StatusChange(
    id = id,
    issueId = issue.id,
    performedBy = performedBy.toModel(),
    createdAt = createdAtUtc.toUtcOffsetDateTime(),
    statusFrom = statusFrom,
    statusTo = requireNotNull(statusTo) { "statusTo is required for STATUS_CHANGE history id=$id" },
)

private fun IssueHistoryEntity.toAssigneeChange(objectMapper: ObjectMapper): IssueHistoryModel.AssigneeChange {
    val assigneeDetails = requiredDetails(objectMapper, AssigneeChangeDetails::class.java)
    return IssueHistoryModel.AssigneeChange(
        id = id,
        issueId = issue.id,
        performedBy = performedBy.toModel(),
        createdAt = createdAtUtc.toUtcOffsetDateTime(),
        assigneeFrom = assigneeDetails.assigneeFrom,
        assigneeTo = assigneeDetails.assigneeTo,
    )
}

private fun IssueHistoryEntity.toComment(objectMapper: ObjectMapper) = IssueHistoryModel.Comment(
    id = id,
    issueId = issue.id,
    performedBy = performedBy.toModel(),
    createdAt = createdAtUtc.toUtcOffsetDateTime(),
    commentText = requiredDetails(objectMapper, CommentDetails::class.java).commentText,
)

private fun IssueHistoryEntity.toTitleChange(objectMapper: ObjectMapper): IssueHistoryModel.TitleChange {
    val titleDetails = requiredDetails(objectMapper, TitleChangeDetails::class.java)
    return IssueHistoryModel.TitleChange(
        id = id,
        issueId = issue.id,
        performedBy = performedBy.toModel(),
        createdAt = createdAtUtc.toUtcOffsetDateTime(),
        titleFrom = titleDetails.titleFrom,
        titleTo = titleDetails.titleTo,
    )
}

private fun IssueHistoryEntity.toDescriptionChange(objectMapper: ObjectMapper): IssueHistoryModel.DescriptionChange {
    val descriptionDetails = requiredDetails(objectMapper, DescriptionChangeDetails::class.java)
    return IssueHistoryModel.DescriptionChange(
        id = id,
        issueId = issue.id,
        performedBy = performedBy.toModel(),
        createdAt = createdAtUtc.toUtcOffsetDateTime(),
        descriptionFrom = descriptionDetails.descriptionFrom,
        descriptionTo = descriptionDetails.descriptionTo,
    )
}

private fun IssueHistoryEntity.toPriorityChange(objectMapper: ObjectMapper): IssueHistoryModel.PriorityChange {
    val priorityDetails = requiredDetails(objectMapper, PriorityChangeDetails::class.java)
    return IssueHistoryModel.PriorityChange(
        id = id,
        issueId = issue.id,
        performedBy = performedBy.toModel(),
        createdAt = createdAtUtc.toUtcOffsetDateTime(),
        priorityFrom = priorityDetails.priorityFrom,
        priorityTo = priorityDetails.priorityTo,
    )
}

private fun IssueHistoryEntity.toVehicleChange(objectMapper: ObjectMapper): IssueHistoryModel.VehicleChange {
    val vehicleDetails = requiredDetails(objectMapper, VehicleChangeDetails::class.java)
    return IssueHistoryModel.VehicleChange(
        id = id,
        issueId = issue.id,
        performedBy = performedBy.toModel(),
        createdAt = createdAtUtc.toUtcOffsetDateTime(),
        vehicleFrom = vehicleDetails.vehicleFrom,
        vehicleTo = vehicleDetails.vehicleTo,
    )
}

private fun <T : Any> IssueHistoryEntity.requiredDetails(objectMapper: ObjectMapper, detailsType: Class<T>): T =
    objectMapper.readValue(requireNotNull(details) { "details are required for $type history id=$id" }, detailsType)
