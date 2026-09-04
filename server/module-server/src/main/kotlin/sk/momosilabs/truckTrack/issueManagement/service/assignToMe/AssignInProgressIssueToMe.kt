package sk.momosilabs.truckTrack.issueManagement.service.assignToMe

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import sk.momosilabs.truckTrack.account.model.AccountModel
import sk.momosilabs.truckTrack.config.GlobalUnprocessableException
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
class AssignInProgressIssueToMe(
    private val issuePersistence: IssuePersistence,
    private val currentUserService: CurrentUserService,
) : AssignInProgressIssueToMeUseCase {

    @IsMechanic
    @Transactional
    override fun reassign(issueId: Long): IssueModel {
        val issue = issuePersistence.findById(issueId)
        if (issue.status != IssueStatus.IN_PROGRESS) {
            throw GlobalUnprocessableException("Issue must be IN_PROGRESS to change currently assigned mechanic.")
        }

        val mechanic: AccountModel = currentUserService.currentUser()
        if (issue.assignedTo?.id == mechanic.id) {
            throw GlobalUnprocessableException("Issue is already assigned to you.")
        }

        val now = OffsetDateTime.now(ZoneOffset.UTC)
        val saved = issuePersistence.updateStatusAndAssignee(issueId, issue.status, mechanic.id, now)
        issuePersistence.saveHistory(
            IssueHistoryModel.AssigneeChange(
                id = UUID.randomUUID(),
                issueId = saved.id,
                performedBy = mechanic,
                createdAt = now,
            )
        )
        return saved
    }
}
