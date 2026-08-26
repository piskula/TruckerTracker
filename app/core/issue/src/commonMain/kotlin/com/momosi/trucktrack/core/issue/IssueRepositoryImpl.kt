package com.momosi.trucktrack.core.issue

import androidx.paging.PagingSource
import com.momosi.trucktrack.core.common.coroutines.runCatchingCancellable
import com.momosi.trucktrack.core.common.logger.Logger
import com.momosi.trucktrack.core.common.model.Page
import com.momosi.trucktrack.core.common.network.onNetworkFailure
import com.momosi.trucktrack.core.common.network.onNoConnectionFailure
import com.momosi.trucktrack.core.issue.api.IssueApi
import com.momosi.trucktrack.core.issue.api.IssueHistoryApi
import com.momosi.trucktrack.core.issue.dto.toDto
import com.momosi.trucktrack.core.issue.dto.toFilterDto
import com.momosi.trucktrack.core.issue.dto.toIssue
import com.momosi.trucktrack.core.issue.dto.toIssueHistory
import com.momosi.trucktrack.core.issue.model.Issue
import com.momosi.trucktrack.core.issue.model.IssueCreate
import com.momosi.trucktrack.core.issue.model.IssueHistory
import com.momosi.trucktrack.core.issue.model.IssueStatus
import com.momosi.trucktrack.core.network.dto.toPage

internal const val TAG = "Issues"

class IssueRepositoryImpl(private val issueApi: IssueApi, private val issueHistoryApi: IssueHistoryApi) : IssueRepository {

    override suspend fun getIssues(
        statuses: List<IssueStatus>,
        vehicleIds: List<Long>,
        accountIds: List<String>,
        page: Int?,
        size: Int?,
    ): Result<Page<Issue>> = runCatchingCancellable {
        issueApi.getIssueList(
            filter = statuses.toFilterDto(vehicleIds, accountIds),
            page = 0,
            size = 500,
            sort = DEFAULT_ISSUE_SORT,
        ).toPage { it.toIssue() }
    }
        .onNoConnectionFailure { Logger.w(TAG, it, "Failed to get issues (offline)") }
        .onNetworkFailure { Logger.e(TAG, it, "Failed to get issues") }

    override suspend fun getIssue(id: Long): Result<Issue> = runCatchingCancellable {
        issueApi.getIssue(id).toIssue()
    }
        .onNoConnectionFailure { Logger.w(TAG, it, "Failed to get issue $id (offline)") }
        .onNetworkFailure { Logger.e(TAG, it, "Failed to get issue $id") }

    override suspend fun createIssue(issueCreate: IssueCreate): Result<Issue> = runCatchingCancellable {
        issueApi.createIssue(issueCreate.toDto()).toIssue()
    }
        .onNoConnectionFailure { Logger.w(TAG, it, "Failed to create issue (offline)") }
        .onNetworkFailure { Logger.e(TAG, it, "Failed to create issue") }

    override suspend fun startIssue(id: Long): Result<Issue> = runCatchingCancellable {
        issueApi.startIssue(id).toIssue()
    }
        .onNoConnectionFailure { Logger.w(TAG, it, "Failed to start issue $id (offline)") }
        .onNetworkFailure { Logger.e(TAG, it, "Failed to start issue $id") }

    override suspend fun resolveIssue(id: Long): Result<Issue> = runCatchingCancellable {
        issueApi.resolveIssue(id).toIssue()
    }
        .onNoConnectionFailure { Logger.w(TAG, it, "Failed to resolve issue $id (offline)") }
        .onNetworkFailure { Logger.e(TAG, it, "Failed to resolve issue $id") }

    override suspend fun assignIssue(id: Long): Result<Issue> = runCatchingCancellable {
        issueApi.assignIssue(id).toIssue()
    }
        .onNoConnectionFailure { Logger.w(TAG, it, "Failed to assign issue $id (offline)") }
        .onNetworkFailure { Logger.e(TAG, it, "Failed to assign issue $id") }

    override suspend fun addComment(issueId: Long, comment: String): Result<IssueHistory> = runCatchingCancellable {
        issueApi.addComment(issueId, comment).toIssueHistory()
    }
        .onNoConnectionFailure { Logger.w(TAG, it, "Failed to add comment to issue $issueId (offline)") }
        .onNetworkFailure { Logger.e(TAG, it, "Failed to add comment to issue $issueId") }

    override suspend fun getIssueHistory(
        issueId: Long,
        page: Int?,
        size: Int?,
    ): Result<Page<IssueHistory>> = runCatchingCancellable {
        issueHistoryApi.getIssueHistory(
            id = issueId,
            page = 0,
            size = 500,
            sort = "createdAtUtc,asc",
        ).toPage { it.toIssueHistory() }
    }
        .onNoConnectionFailure { Logger.w(TAG, it, "Failed to get issue history for issue $issueId (offline)") }
        .onNetworkFailure { Logger.e(TAG, it, "Failed to get issue history for issue $issueId") }

    override fun getIssuesPagingSource(
        statuses: List<IssueStatus>,
        vehicleIds: List<Long>,
        accountIds: List<String>,
        sort: String,
    ): PagingSource<Int, Issue> = IssuePagingSource(
        issueApi = issueApi,
        statuses = statuses,
        vehicleIds = vehicleIds,
        accountIds = accountIds,
        sort = sort,
    )
}
