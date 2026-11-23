package com.momosi.trucktrack.core.applinks

sealed interface AppLinkAction {
    data class Issue(val issueId: Long) : AppLinkAction
}
