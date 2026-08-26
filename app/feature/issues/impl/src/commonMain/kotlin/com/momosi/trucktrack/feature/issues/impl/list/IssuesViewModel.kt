package com.momosi.trucktrack.feature.issues.impl.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.momosi.trucktrack.core.common.logger.Logger
import com.momosi.trucktrack.core.issue.DEFAULT_ISSUE_SORT
import com.momosi.trucktrack.core.issue.IssueRepository
import com.momosi.trucktrack.core.issue.model.Issue
import com.momosi.trucktrack.core.issue.model.IssueStatus
import com.momosi.trucktrack.user.UserRepository
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn

@OptIn(ExperimentalCoroutinesApi::class)
class IssuesViewModel(private val userRepository: UserRepository, private val issueRepository: IssueRepository) : ViewModel() {

    private val initialFilter: IssueFilter = run {
        val userRoles = userRepository.user.value?.roles ?: emptySet()
        IssueFilter.entries.firstOrNull { filter ->
            userRoles.any { filtersByRole[it]?.contains(filter) == true }
        } ?: IssueFilter.All
    }
    private val selectedFilter = MutableStateFlow<IssueFilter>(initialFilter)
    private val refreshTrigger = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    val pagingData: Flow<PagingData<Issue>> =
        combine(
            selectedFilter,
            refreshTrigger.onStart { emit(Unit) },
            userRepository.user,
        ) { filter, _, user ->
            Pair(filter, user?.id)
        }.flatMapLatest { (filter, userId) ->
            Pager(
                config = PagingConfig(
                    pageSize = 14,
                    enablePlaceholders = false,
                ),
                pagingSourceFactory = {
                    issueRepository.getIssuesPagingSource(
                        statuses = filter.statuses(),
                        accountIds = filter.accountIds(userId),
                        sort = filter.sort(),
                    )
                },
            ).flow
        }.cachedIn(viewModelScope)

    val state: StateFlow<IssuesState> = combine(
        userRepository.user,
        selectedFilter,
    ) { user, filter ->
        IssuesState(
            userInfo = user?.let {
                IssuesUserInfo(
                    name = it.name,
                    roles = it.roles.toImmutableList(),
                )
            },
            selectedFilter = filter,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = IssuesState(),
    )

    fun onAction(action: IssuesAction) {
        Logger.i("Action:Issues", action.toString())
        when (action) {
            is IssuesAction.SelectFilter -> selectedFilter.value = action.filter
            is IssuesAction.Retry -> refreshTrigger.tryEmit(Unit)
            is IssuesAction.Refresh -> refreshTrigger.tryEmit(Unit)
            is IssuesAction.OpenIssue -> Unit
            is IssuesAction.CreateIssue -> Unit
        }
    }
}

private fun IssueFilter.statuses(): List<IssueStatus> = when (this) {
    IssueFilter.MyIssues -> listOf(IssueStatus.Open, IssueStatus.InProgress)
    IssueFilter.MyResolved -> listOf(IssueStatus.Done)
    IssueFilter.MyWork -> listOf(IssueStatus.InProgress)
    IssueFilter.MyCompleted -> listOf(IssueStatus.Done)
    IssueFilter.Open -> listOf(IssueStatus.Open)
    IssueFilter.All -> emptyList()
}

private fun IssueFilter.sort(): String = when (this) {
    IssueFilter.MyCompleted,
    IssueFilter.MyResolved,
    -> "updatedAtUtc,desc"

    IssueFilter.MyIssues,
    IssueFilter.MyWork,
    IssueFilter.Open,
    IssueFilter.All,
    -> DEFAULT_ISSUE_SORT
}

private fun IssueFilter.accountIds(userId: String?): List<String> = when (this) {
    IssueFilter.MyIssues,
    IssueFilter.MyResolved,
    IssueFilter.MyWork,
    IssueFilter.MyCompleted,
    -> listOfNotNull(userId)

    IssueFilter.Open,
    IssueFilter.All,
    -> emptyList()
}
