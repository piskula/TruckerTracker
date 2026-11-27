package com.momosi.trucktrack.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation3.runtime.NavKey
import com.momosi.trucktrack.core.applinks.AppLinkAction
import com.momosi.trucktrack.core.applinks.AppLinksManager
import com.momosi.trucktrack.core.checklist.ChecklistRepository
import com.momosi.trucktrack.core.checklist.model.ChecklistStatus
import com.momosi.trucktrack.core.common.error.ErrorReporter
import com.momosi.trucktrack.core.push.permission.NotificationPermission
import com.momosi.trucktrack.feature.issues.api.IssuesNavKey
import com.momosi.trucktrack.user.AuthManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TruckTrackViewModel(authManager: AuthManager, errorReporter: ErrorReporter, appLinks: AppLinksManager, private val notificationPermission: NotificationPermission, private val checklistRepository: ChecklistRepository) : ViewModel() {

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

    private val checklistRequests = Channel<Unit>(Channel.CONFLATED)
    internal val openChecklistRequests: Flow<Unit> = checklistRequests.receiveAsFlow()
    private var leftIssuesListDuringCheck = false

    init {
        refreshNotificationPermission()
        viewModelScope.launch { access.collectLatest(::onAccessChange) }
    }

    fun onResume() {
        refreshNotificationPermission()
    }

    internal fun onScreenChange(key: NavKey) {
        if (key != IssuesNavKey) leftIssuesListDuringCheck = true
    }

    private suspend fun onAccessChange(access: AppAccess) {
        when (access) {
            AppAccess.Ready -> openChecklistIfPending()
            AppAccess.SignedOut -> checklistRepository.clearStatus()
            AppAccess.Unknown, AppAccess.NotificationPermissionRequired -> Unit
        }
    }

    private suspend fun openChecklistIfPending() {
        leftIssuesListDuringCheck = false
        val isPending = checklistRepository.refreshStatus().getOrNull() == ChecklistStatus.Pending
        if (isPending && !leftIssuesListDuringCheck) {
            checklistRequests.send(Unit)
        }
    }

    private fun refreshNotificationPermission() {
        viewModelScope.launch { notificationPermission.status() }
    }
}
