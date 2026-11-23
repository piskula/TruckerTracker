package com.momosi.trucktrack.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.momosi.trucktrack.core.applinks.AppLinkAction
import com.momosi.trucktrack.core.applinks.AppLinksManager
import com.momosi.trucktrack.core.common.error.ErrorReporter
import com.momosi.trucktrack.core.push.permission.NotificationPermission
import com.momosi.trucktrack.user.AuthManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TruckTrackViewModel(authManager: AuthManager, errorReporter: ErrorReporter, appLinks: AppLinksManager, private val notificationPermission: NotificationPermission) : ViewModel() {

    internal val access: StateFlow<AppAccess> = combine(
        authManager.authenticationState,
        notificationPermission.currentStatus,
        ::appAccessOf,
    ).stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = appAccessOf(authManager.authenticationState.value, notificationPermission.currentStatus.value),
    )

    val errors: Flow<Throwable> = errorReporter.errors

    @OptIn(ExperimentalCoroutinesApi::class)
    internal val appLinkActions: Flow<AppLinkAction> = access.flatMapLatest { access ->
        if (access == AppAccess.Ready) appLinks.actions else emptyFlow()
    }

    init {
        refreshNotificationPermission()
    }

    fun refreshNotificationPermission() {
        viewModelScope.launch { notificationPermission.status() }
    }
}
