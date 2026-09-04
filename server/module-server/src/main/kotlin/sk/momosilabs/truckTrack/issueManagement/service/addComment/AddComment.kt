package sk.momosilabs.truckTrack.issueManagement.service.addComment

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import sk.momosilabs.truckTrack.config.GlobalUnprocessableException
import sk.momosilabs.truckTrack.issueManagement.entity.IssueStatus
import sk.momosilabs.truckTrack.issueManagement.model.IssueHistoryModel
import sk.momosilabs.truckTrack.issueManagement.service.IssuePersistence
import sk.momosilabs.truckTrack.security.CurrentUserService
import sk.momosilabs.truckTrack.security.annotation.IsUser
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.UUID

@Service
class AddComment(
    private val issuePersistence: IssuePersistence,
    private val currentUserService: CurrentUserService,
) : AddCommentUseCase {

    @IsUser
    @Transactional
    override fun addComment(issueId: Long, comment: String): IssueHistoryModel {
        val issue = issuePersistence.findById(issueId)
        if (issue.status == IssueStatus.DONE) {
            throw GlobalUnprocessableException("Cannot add a comment to a DONE issue, current status: ${issue.status}")
        }

        return issuePersistence.saveHistory(
            IssueHistoryModel.Comment(
                id = UUID.randomUUID(),
                issueId = issueId,
                performedBy = currentUserService.currentUser(),
                createdAt = OffsetDateTime.now(),
                commentText = comment,
            )
        )
    }
}
