package com.momosi.trucktrack.feature.issues.impl.edit

sealed interface EditIssueEvent {
    data object IssueUpdated : EditIssueEvent
}
