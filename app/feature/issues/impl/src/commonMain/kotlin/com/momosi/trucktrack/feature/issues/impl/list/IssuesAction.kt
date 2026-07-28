package com.momosi.trucktrack.feature.issues.impl.list

import com.momosi.trucktrack.user.model.UserRole

sealed interface IssuesAction {
    data class SelectFilter(val filter: IssueFilter) : IssuesAction
    data class OpenIssue(val issueId: Long) : IssuesAction
    data object CreateIssue : IssuesAction
    data object Retry : IssuesAction
    data object Refresh : IssuesAction
}

enum class IssueFilter {
    MyIssues,
    MyResolved,
    MyWork,
    MyCompleted,
    Open,
    All,
}

val filtersByRole: Map<UserRole, Set<IssueFilter>> = mapOf(
    UserRole.Driver to setOf(
        IssueFilter.MyIssues,
        IssueFilter.MyResolved,
        IssueFilter.All,
    ),
    UserRole.Mechanic to setOf(
        IssueFilter.MyWork,
        IssueFilter.MyCompleted,
        IssueFilter.Open,
        IssueFilter.All,
    ),
)
