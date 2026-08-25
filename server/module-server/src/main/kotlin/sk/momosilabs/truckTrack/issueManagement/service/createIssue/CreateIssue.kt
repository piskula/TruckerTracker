package sk.momosilabs.truckTrack.issueManagement.service.createIssue

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import sk.momosilabs.truckTrack.account.model.AccountModel
import sk.momosilabs.truckTrack.issueManagement.entity.IssueHistoryEventType
import sk.momosilabs.truckTrack.issueManagement.entity.IssueStatus
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
class CreateIssue(
    private val issuePersistence: IssuePersistence,
    private val vehiclePersistence: VehiclePersistence,
    private val currentUserService: CurrentUserService,
) : CreateIssueUseCase {

    @IsUser
    @Transactional
    override fun create(command: CreateIssueCommand): IssueModel {
        val vehicle = vehiclePersistence.findById(command.vehicleId)
        val reportedBy: AccountModel = currentUserService.currentUser()

        val now = OffsetDateTime.now(ZoneOffset.UTC)
        val saved = issuePersistence.create(
            IssueModel(
                title = command.title,
                description = command.description,
                status = IssueStatus.OPEN,
                priority = command.priority,
                vehicle = vehicle,
                reportedBy = reportedBy,
                assignedTo = null,
                createdAt = now,
                updatedAt = now,
            )
        )
        issuePersistence.saveHistory(
            IssueHistoryModel(
                id = UUID.randomUUID(),
                issueId = saved.id,
                type = IssueHistoryEventType.STATUS_CHANGE,
                performedBy = reportedBy,
                createdAt = now,
                statusFrom = null,
                statusTo = IssueStatus.OPEN,
                commentText = null,
            )
        )
        return saved
    }
}
