package sk.momosilabs.truckTrack.api.issue

import sk.momosilabs.truckTrack.api.common.PageDto
import sk.momosilabs.truckTrack.api.common.PageableDto
import sk.momosilabs.truckTrack.api.issue.dto.IssueCreateDto
import sk.momosilabs.truckTrack.api.issue.dto.IssueDto
import sk.momosilabs.truckTrack.api.issue.dto.IssueFilterDto
import sk.momosilabs.truckTrack.api.issue.dto.IssueHistoryDto
import sk.momosilabs.truckTrack.api.issue.dto.IssueUpdateDto
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springdoc.core.annotations.ParameterObject
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody

@Tag(name = "Issues")
interface IssueManagementApi {

    companion object {
        private const val ENDPOINT = "/api/v1/issue"
    }

    @Operation(summary = "Get issue list")
    @PostMapping(ENDPOINT)
    fun getIssueList(
        @RequestBody(required = false) filter: IssueFilterDto?,
        @ParameterObject pageable: PageableDto,
    ): PageDto<IssueDto>

    @Operation(summary = "Get issue by ID")
    @GetMapping("$ENDPOINT/byId/{id}")
    fun getIssue(@PathVariable id: Long): IssueDto

    @Operation(summary = "Create issue")
    @PostMapping("$ENDPOINT/create")
    fun createIssue(@RequestBody request: IssueCreateDto): IssueDto

    @Operation(summary = "Update issue")
    @PatchMapping("$ENDPOINT/{id}")
    fun updateIssue(@PathVariable id: Long, @RequestBody request: IssueUpdateDto): IssueDto

    @Operation(summary = "Start issue")
    @PostMapping("$ENDPOINT/{id}/start")
    fun startIssue(@PathVariable id: Long): IssueDto

    @Operation(summary = "Assign in-progress issue to me")
    @PostMapping("$ENDPOINT/{id}/assignTome")
    fun assignToMe(@PathVariable id: Long): IssueDto

    @Operation(summary = "Resolve issue")
    @PostMapping("$ENDPOINT/{id}/resolve")
    fun resolveIssue(@PathVariable id: Long): IssueDto

    @Operation(summary = "Cancel issue")
    @PostMapping("$ENDPOINT/{id}/cancel")
    fun cancelIssue(@PathVariable id: Long): IssueDto

    @Operation(summary = "Get issue history")
    @GetMapping("$ENDPOINT/{id}/history")
    fun getIssueHistory(
        @PathVariable id: Long,
        @ParameterObject pageable: PageableDto,
    ): PageDto<IssueHistoryDto>

    @Operation(summary = "Add comment")
    @PostMapping("$ENDPOINT/{id}/comment")
    fun addComment(@PathVariable id: Long, @RequestBody text: String): IssueHistoryDto
}
