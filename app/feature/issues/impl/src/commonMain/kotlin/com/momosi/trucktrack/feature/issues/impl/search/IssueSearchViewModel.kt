package com.momosi.trucktrack.feature.issues.impl.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.momosi.trucktrack.core.common.logger.Logger
import com.momosi.trucktrack.core.common.network.ApiException
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

@OptIn(ExperimentalCoroutinesApi::class)
class IssueSearchViewModel(private val issueRepository: IssueRepository) : ViewModel() {

    private val query = MutableStateFlow("")

    private val content: Flow<IssueSearchContent> = query.flatMapLatest { currentQuery ->
        val issueId = currentQuery.toLongOrNull()
        if (issueId == null) {
            flowOf(IssueSearchContent.Blank)
        } else {
            flow {
                emit(IssueSearchContent.Loading)
                delay(SEARCH_DEBOUNCE_MILLIS)
                emit(searchIssue(issueId))
            }
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

    private suspend fun searchIssue(issueId: Long): IssueSearchContent = issueRepository.getIssue(issueId).fold(
        onSuccess = { IssueSearchContent.Found(it) },
        onFailure = { error ->
            if (error is ApiException.HttpError && error.statusCode == HTTP_NOT_FOUND) {
                IssueSearchContent.NotFound
            } else {
                Logger.e("IssueSearch", error, "Failed to search issue $issueId")
                IssueSearchContent.Error
            }
        },
    )
}

private fun String.toIssueIdQuery(): String = filter { it.isDigit() }.take(MAX_QUERY_LENGTH)

private const val SEARCH_DEBOUNCE_MILLIS = 300L
private const val MAX_QUERY_LENGTH = 18
private const val HTTP_NOT_FOUND = 404
