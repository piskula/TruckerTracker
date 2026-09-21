package com.momosi.trucktrack.feature.issues.impl.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.momosi.trucktrack.core.common.logger.Logger
import com.momosi.trucktrack.core.common.network.ApiException
import com.momosi.trucktrack.core.common.network.isNotFound
import com.momosi.trucktrack.core.issue.IssueRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalCoroutinesApi::class)
class IssueSearchViewModel(private val issueRepository: IssueRepository) : ViewModel() {

    private val query = MutableStateFlow("")
    private val resultCache = mutableMapOf<Long, IssueSearchContent>()

    private val content: Flow<IssueSearchContent> = query.flatMapLatest { currentQuery ->
        val issueId = currentQuery.toLongOrNull()
        if (issueId == null) {
            flowOf(IssueSearchContent.Blank)
        } else {
            debouncedSearch(issueId)
        }
    }

    val state: StateFlow<IssueSearchState> = combine(query, content) { currentQuery, currentContent ->
        IssueSearchState(query = currentQuery, content = currentContent)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = IssueSearchState(),
    )

    fun onAction(action: IssueSearchAction) {
        Logger.i("Action:IssueSearch", action.toString())
        when (action) {
            is IssueSearchAction.ChangeQuery -> query.value = action.query.toIssueIdQuery()
        }
    }

    private fun debouncedSearch(issueId: Long): Flow<IssueSearchContent> = flow {
        val cached = resultCache[issueId]
        if (cached != null) {
            emit(cached)
            return@flow
        }

        emit(IssueSearchContent.Loading)
        delay(SEARCH_DEBOUNCE_MILLIS.milliseconds)
        val result = searchIssue(issueId)
        if (result !is IssueSearchContent.Error) {
            resultCache[issueId] = result
        }
        emit(result)
    }

    private suspend fun searchIssue(issueId: Long): IssueSearchContent = issueRepository.getIssue(issueId).fold(
        onSuccess = { IssueSearchContent.Found(it) },
        onFailure = { error ->
            if (error is ApiException && error.isNotFound()) {
                IssueSearchContent.NotFound
            } else {
                Logger.e("IssueSearch", error, "Failed to search issue $issueId")
                IssueSearchContent.Error
            }
        },
    )
}

private fun String.toIssueIdQuery(): String = filter { it.isDigit() }.take(MAX_ISSUE_ID_DIGITS)

private const val SEARCH_DEBOUNCE_MILLIS = 300L
private const val MAX_ISSUE_ID_DIGITS = 18
