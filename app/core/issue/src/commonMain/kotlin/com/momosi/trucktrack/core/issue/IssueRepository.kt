package com.momosi.trucktrack.core.issue

import androidx.paging.PagingSource
import com.momosi.trucktrack.core.common.model.Page
import com.momosi.trucktrack.core.issue.model.Issue
import com.momosi.trucktrack.core.issue.model.IssueCreate
import com.momosi.trucktrack.core.issue.model.IssueHistory
import com.momosi.trucktrack.core.issue.model.IssueStatus

interface IssueRepository {

    suspend fun getIssues(
        statuses: List<IssueStatus> = emptyList(),
        vehicleIds: List<Long> = emptyList(),
        accountIds: List<String> = emptyList(),
        page: Int? = null,
        size: Int? = null,
    ): Result<Page<Issue>>

    suspend fun getIssue(id: Long): Result<Issue>

    suspend fun createIssue(issueCreate: IssueCreate): Result<Issue>

    suspend fun startIssue(id: Long): Result<Issue>

    suspend fun resolveIssue(id: Long): Result<Issue>

    suspend fun cancelIssue(id: Long): Result<Issue>

    suspend fun assignIssue(id: Long): Result<Issue>

    suspend fun addComment(issueId: Long, comment: String): Result<IssueHistory>

    suspend fun getIssueHistory(
        issueId: Long,
        page: Int? = null,
        size: Int? = null,
    ): Result<Page<IssueHistory>>

    fun getIssuesPagingSource(
        statuses: List<IssueStatus> = emptyList(),
        vehicleIds: List<Long> = emptyList(),
        accountIds: List<String> = emptyList(),
    ): PagingSource<Int, Issue>
}
