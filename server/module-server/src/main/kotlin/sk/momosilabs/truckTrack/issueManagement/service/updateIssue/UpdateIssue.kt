package sk.momosilabs.truckTrack.issueManagement.service.updateIssue

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import sk.momosilabs.truckTrack.account.model.AccountModel
import sk.momosilabs.truckTrack.config.GlobalForbiddenException
import sk.momosilabs.truckTrack.config.GlobalUnprocessableException
import sk.momosilabs.truckTrack.issueManagement.entity.IssueStatus
import sk.momosilabs.truckTrack.issueManagement.entity.IssueUpdatedField
import sk.momosilabs.truckTrack.issueManagement.model.IssueHistoryModel
import sk.momosilabs.truckTrack.issueManagement.model.IssueModel
import sk.momosilabs.truckTrack.issueManagement.service.IssuePersistence
import sk.momosilabs.truckTrack.security.CurrentUserService
import sk.momosilabs.truckTrack.security.annotation.IsUser
import sk.momosilabs.truckTrack.vehicle.model.VehicleModel
import sk.momosilabs.truckTrack.vehicle.service.VehiclePersistence
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.UUID

@Service
class UpdateIssue(
    private val issuePersistence: IssuePersistence,
    private val vehiclePersistence: VehiclePersistence,
    private val currentUserService: CurrentUserService,
) : UpdateIssueUseCase {

    @IsUser
    @Transactional
    override fun update(issueId: Long, command: UpdateIssueCommand): IssueModel {
        val issue = issuePersistence.findByIdOrThrow(issueId)
        val currentUser: AccountModel = currentUserService.currentUser()

        if (issue.status.isClosed()) {
            throw GlobalUnprocessableException("Issue must not be DONE or CANCELED to update, current status: ${issue.status}")
        }

        val vehicle = vehiclePersistence.findById(command.vehicleId)
        val changedFields = buildList {
            if (issue.title != command.title) add(IssueUpdatedField.TITLE)
            if (issue.description != command.description) add(IssueUpdatedField.DESCRIPTION)
            if (issue.priority != command.priority) add(IssueUpdatedField.PRIORITY)
            if (issue.vehicle.id != vehicle.id) add(IssueUpdatedField.VEHICLE)
        }
        if (changedFields.isEmpty()) {
            return issue
        }

        val editableFields = editableFields(issue, currentUser.id, currentUserService.isMechanic())
        val forbiddenFields = changedFields - editableFields
        if (forbiddenFields.isNotEmpty()) {
            throw GlobalForbiddenException("You are not allowed to change $forbiddenFields of this issue in status ${issue.status}")
        }

        val now = OffsetDateTime.now(ZoneOffset.UTC)
        val saved = issuePersistence.update(
            issue.copy(
                title = command.title,
                description = command.description,
                priority = command.priority,
                vehicle = vehicle,
                updatedAt = now,
            )
        )
        saveHistory(issue, saved, vehicle, changedFields, currentUser, now)
        return saved
    }

    private fun editableFields(issue: IssueModel, currentUserId: UUID, isMechanic: Boolean): Set<IssueUpdatedField> {
        val isReporter = issue.reportedBy.id == currentUserId
        val isAssignedMechanic = isMechanic && issue.assignedTo?.id == currentUserId
        val isOpen = issue.status == IssueStatus.OPEN
        return buildSet {
            if (isReporter && isOpen) add(IssueUpdatedField.TITLE)
            if (isReporter && isOpen) add(IssueUpdatedField.DESCRIPTION)
            if ((isReporter && isOpen) || isAssignedMechanic) add(IssueUpdatedField.PRIORITY)
            if ((isReporter && isOpen) || isAssignedMechanic) add(IssueUpdatedField.VEHICLE)
        }
    }

    private fun saveHistory(
        original: IssueModel,
        saved: IssueModel,
        newVehicle: VehicleModel,
        changedFields: List<IssueUpdatedField>,
        performedBy: AccountModel,
        now: OffsetDateTime,
    ) {
        if (IssueUpdatedField.TITLE in changedFields) {
            issuePersistence.saveHistory(
                IssueHistoryModel.TitleChange(
                    id = UUID.randomUUID(),
                    issueId = saved.id,
                    performedBy = performedBy,
                    createdAt = now,
                    titleFrom = original.title,
                    titleTo = saved.title,
                )
            )
        }
        if (IssueUpdatedField.DESCRIPTION in changedFields) {
            issuePersistence.saveHistory(
                IssueHistoryModel.DescriptionChange(
                    id = UUID.randomUUID(),
                    issueId = saved.id,
                    performedBy = performedBy,
                    createdAt = now,
                    descriptionFrom = original.description,
                    descriptionTo = saved.description,
                )
            )
        }
        if (IssueUpdatedField.PRIORITY in changedFields) {
            issuePersistence.saveHistory(
                IssueHistoryModel.PriorityChange(
                    id = UUID.randomUUID(),
                    issueId = saved.id,
                    performedBy = performedBy,
                    createdAt = now,
                    priorityFrom = original.priority,
                    priorityTo = saved.priority,
                )
            )
        }
        if (IssueUpdatedField.VEHICLE in changedFields) {
            issuePersistence.saveHistory(
                IssueHistoryModel.VehicleChange(
                    id = UUID.randomUUID(),
                    issueId = saved.id,
                    performedBy = performedBy,
                    createdAt = now,
                    vehicleFrom = original.vehicle.toSnapshot(),
                    vehicleTo = newVehicle.toSnapshot(),
                )
            )
        }
    }

    private fun VehicleModel.toSnapshot() = IssueHistoryModel.VehicleSnapshot(id = id, licensePlate = licensePlate)
}
