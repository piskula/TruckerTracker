package sk.momosilabs.truckTrack.issueManagement.controller

import sk.momosilabs.truckTrack.api.common.PageDto
import sk.momosilabs.truckTrack.api.common.PageableDto
import sk.momosilabs.truckTrack.api.issue.dto.IssueCreateDto
import sk.momosilabs.truckTrack.api.issue.dto.IssueDto
import sk.momosilabs.truckTrack.api.issue.dto.IssueFilterDto
import sk.momosilabs.truckTrack.api.issue.dto.IssueHistoryDto
import sk.momosilabs.truckTrack.api.issue.dto.IssueHistoryPageDto
import sk.momosilabs.truckTrack.api.issue.dto.IssueUpdateDto
import org.springframework.web.bind.annotation.RestController
import sk.momosilabs.truckTrack.api.issue.IssueManagementApi
import sk.momosilabs.truckTrack.issueManagement.service.addComment.AddCommentUseCase
import sk.momosilabs.truckTrack.issueManagement.service.assignToMe.AssignInProgressIssueToMeUseCase
import sk.momosilabs.truckTrack.issueManagement.service.cancelIssue.CancelIssueUseCase
import sk.momosilabs.truckTrack.issueManagement.service.createIssue.CreateIssueCommand
import sk.momosilabs.truckTrack.issueManagement.service.createIssue.CreateIssueUseCase
import sk.momosilabs.truckTrack.issueManagement.service.getIssue.GetIssueUseCase
import sk.momosilabs.truckTrack.issueManagement.service.getIssueHistory.GetIssueHistoryUseCase
import sk.momosilabs.truckTrack.issueManagement.service.getIssueList.GetIssueListUseCase
import sk.momosilabs.truckTrack.issueManagement.service.resolveIssue.ResolveIssueUseCase
import sk.momosilabs.truckTrack.issueManagement.service.startIssue.StartIssueUseCase
import sk.momosilabs.truckTrack.issueManagement.service.updateIssue.UpdateIssueCommand
import sk.momosilabs.truckTrack.issueManagement.service.updateIssue.UpdateIssueUseCase
import sk.momosilabs.truckTrack.util.toDto
import sk.momosilabs.truckTrack.util.toModel

@RestController
class IssueManagementController(
    private val getIssueList: GetIssueListUseCase,
    private val getIssue: GetIssueUseCase,
    private val createIssue: CreateIssueUseCase,
    private val updateIssue: UpdateIssueUseCase,
    private val startIssue: StartIssueUseCase,
    private val assignInProgressIssueToMe: AssignInProgressIssueToMeUseCase,
    private val resolveIssue: ResolveIssueUseCase,
    private val cancelIssue: CancelIssueUseCase,
    private val getIssueHistory: GetIssueHistoryUseCase,
    private val addComment: AddCommentUseCase,
) : IssueManagementApi {

    override fun getIssueList(
        filter: IssueFilterDto?,
        pageable: PageableDto
    ): PageDto<IssueDto> =
        getIssueList.get(filter?.toModel(), pageable.toModel())
            .toDto { it.toDto() }

    override fun getIssue(id: Long): IssueDto =
        getIssue.get(id).toDto()

    override fun createIssue(request: IssueCreateDto): IssueDto =
        createIssue.create(
            CreateIssueCommand(
                vehicleId = request.vehicleId,
                title = request.title,
                description = request.description,
                priority = request.priority.toModel(),
            )
        ).toDto()

    override fun updateIssue(id: Long, request: IssueUpdateDto): IssueDto =
        updateIssue.update(
            issueId = id,
            command = UpdateIssueCommand(
                vehicleId = request.vehicleId,
                title = request.title,
                description = request.description,
                priority = request.priority.toModel(),
            )
        ).toDto()

    override fun startIssue(id: Long): IssueDto =
        startIssue.start(id).toDto()

    override fun assignToMe(id: Long): IssueDto =
        assignInProgressIssueToMe.reassign(issueId = id).toDto()

    override fun resolveIssue(id: Long): IssueDto =
        resolveIssue.resolve(id).toDto()

    override fun cancelIssue(id: Long): IssueDto =
        cancelIssue.cancel(id).toDto()

    override fun getIssueHistory(id: Long, pageable: PageableDto): IssueHistoryPageDto {
        val page = getIssueHistory.get(id, pageable.toModel())
        return IssueHistoryPageDto(
            totalElements = page.totalElements,
            totalPages = page.totalPages,
            number = page.number,
            size = page.size,
            numberOfElements = page.numberOfElements,
            content = page.content.map { it.toDto() },
        )
    }

    override fun addComment(id: Long, text: String): IssueHistoryDto =
        addComment.addComment(issueId = id, comment = text).toDto()

}
