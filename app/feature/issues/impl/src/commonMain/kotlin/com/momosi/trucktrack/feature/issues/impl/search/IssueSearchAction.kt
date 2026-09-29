package com.momosi.trucktrack.feature.issues.impl.search

sealed interface IssueSearchAction {
    data class ChangeQuery(val query: String) : IssueSearchAction
}
