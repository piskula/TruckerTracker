package com.momosi.trucktrack.feature.issues.impl.search

import androidx.compose.runtime.Immutable
import com.momosi.trucktrack.core.issue.model.Issue

@Immutable
data class IssueSearchState(val query: String = "", val content: IssueSearchContent = IssueSearchContent.Blank)

@Immutable
sealed interface IssueSearchContent {
    data object Blank : IssueSearchContent
    data object Loading : IssueSearchContent
    data object NotFound : IssueSearchContent
    data object Error : IssueSearchContent

    @Immutable
    data class Found(val issue: Issue) : IssueSearchContent
}
