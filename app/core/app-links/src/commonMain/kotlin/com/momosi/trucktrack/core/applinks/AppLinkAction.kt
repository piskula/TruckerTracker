package com.momosi.trucktrack.core.applinks

sealed interface AppLinkAction {
    data object Checklist : AppLinkAction

    data class Issue(val issueId: Long) : AppLinkAction
}
