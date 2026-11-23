package com.momosi.trucktrack.core.applinks

import kotlinx.coroutines.flow.Flow

interface AppLinksManager {
    val actions: Flow<AppLinkAction>

    fun openUrl(url: String): Boolean
}
