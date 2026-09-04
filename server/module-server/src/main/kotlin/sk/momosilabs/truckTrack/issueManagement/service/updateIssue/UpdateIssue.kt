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
        val issue = issuePersistence.findById(issueId)
        val currentUser: AccountModel = currentUserService.currentUser()

        if (issue.reportedBy.id != currentUser.id && issue.assignedTo?.id != currentUser.id) {
            throw GlobalForbiddenException("Only the reporter or the assigned mechanic can update this issue")
        }
        if (issue.status == IssueStatus.DONE || issue.status == IssueStatus.CANCELED) {
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
        issuePersistence.saveHistory(
            IssueHistoryModel.Update(
                id = UUID.randomUUID(),
                issueId = saved.id,
                performedBy = currentUser,
                createdAt = now,
                changedFields = changedFields,
            )
        )
        return saved
    }
}
