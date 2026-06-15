package sk.momosilabs.truckTrack.issueManagement.service.startIssue

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import sk.momosilabs.truckTrack.account.model.AccountModel
import sk.momosilabs.truckTrack.config.GlobalUnprocessableException
import sk.momosilabs.truckTrack.issueManagement.entity.IssueHistoryEventType
import sk.momosilabs.truckTrack.issueManagement.entity.IssueStatus
import sk.momosilabs.truckTrack.issueManagement.model.IssueHistoryModel
import sk.momosilabs.truckTrack.issueManagement.model.IssueModel
import sk.momosilabs.truckTrack.issueManagement.service.IssuePersistence
import sk.momosilabs.truckTrack.security.CurrentUserService
import sk.momosilabs.truckTrack.security.annotation.IsMechanic
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.UUID

@Service
class StartIssue(
    private val issuePersistence: IssuePersistence,
    private val currentUserService: CurrentUserService,
) : StartIssueUseCase {

    @IsMechanic
    @Transactional
    override fun start(issueId: Long): IssueModel {
        val issue = issuePersistence.findById(issueId)
        if (issue.status != IssueStatus.OPEN) {
            throw GlobalUnprocessableException("Issue must be OPEN to start, current status: ${issue.status}")
        }

        val mechanic: AccountModel = currentUserService.currentUser()

        val now = OffsetDateTime.now(ZoneOffset.UTC)
        val saved = issuePersistence.updateStatus(issueId, IssueStatus.IN_PROGRESS, now)
        issuePersistence.saveHistory(
            IssueHistoryModel(
                id = UUID.randomUUID(),
                issueId = saved.id,
                type = IssueHistoryEventType.STATUS_CHANGE,
                performedBy = mechanic,
                createdAt = now,
                statusFrom = IssueStatus.OPEN,
                statusTo = IssueStatus.IN_PROGRESS,
                commentText = null,
            )
        )
        return saved
    }
}
