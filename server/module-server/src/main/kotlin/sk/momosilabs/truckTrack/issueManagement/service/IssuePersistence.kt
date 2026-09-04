package sk.momosilabs.truckTrack.issueManagement.service

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import sk.momosilabs.truckTrack.issueManagement.entity.IssueStatus
import sk.momosilabs.truckTrack.issueManagement.model.IssueHistoryModel
import sk.momosilabs.truckTrack.issueManagement.model.IssueModel
import java.time.OffsetDateTime
import java.util.UUID

interface IssuePersistence {

    fun findPage(filter: IssueListFilter, pageable: Pageable): Page<IssueModel>

    fun findById(id: Long): IssueModel

    fun create(model: IssueModel): IssueModel

    fun update(model: IssueModel): IssueModel

    fun updateStatusAndAssignee(id: Long, status: IssueStatus, newAssignee: UUID?, updatedAt: OffsetDateTime): IssueModel

    fun findHistory(issueId: Long, pageable: Pageable): Page<IssueHistoryModel>

    fun saveHistory(model: IssueHistoryModel): IssueHistoryModel
}
