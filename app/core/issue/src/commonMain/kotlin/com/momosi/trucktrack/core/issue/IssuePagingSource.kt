package com.momosi.trucktrack.core.issue

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.momosi.trucktrack.core.common.logger.Logger
import com.momosi.trucktrack.core.issue.api.IssueApi
import com.momosi.trucktrack.core.issue.dto.toFilterDto
import com.momosi.trucktrack.core.issue.dto.toIssue
import com.momosi.trucktrack.core.issue.model.Issue
import com.momosi.trucktrack.core.issue.model.IssueStatus
import kotlinx.coroutines.CancellationException

internal class IssuePagingSource(private val issueApi: IssueApi, private val statuses: List<IssueStatus>, private val vehicleIds: List<Long>, private val accountIds: List<String>) : PagingSource<Int, Issue>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Issue> = try {
        val page = params.key ?: 0
        val pageDto = issueApi.getIssueList(
            filter = statuses.toFilterDto(vehicleIds, accountIds),
            page = page,
            size = params.loadSize,
            sort = "status,desc;priority,asc;createdAtUtc,asc",
        )
        val issues = pageDto.content.map { it.toIssue() }
        LoadResult.Page(
            data = issues,
            prevKey = if (page > 0) page - 1 else null,
            nextKey = if (page < pageDto.totalPages - 1) page + 1 else null,
        )
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Logger.e(TAG, e, "Failed to load issue page")
        LoadResult.Error(e)
    }

    override fun getRefreshKey(state: PagingState<Int, Issue>): Int? = state.anchorPosition?.let { anchor ->
        state.closestPageToPosition(anchor)?.prevKey?.plus(1)
            ?: state.closestPageToPosition(anchor)?.nextKey?.minus(1)
    }
}
