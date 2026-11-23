package com.momosi.trucktrack.feature.notificationpermission.impl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.momosi.trucktrack.core.common.error.ErrorReporter
import com.momosi.trucktrack.core.common.logger.Logger
import com.momosi.trucktrack.core.common.network.ApiException
import com.momosi.trucktrack.core.push.permission.NotificationPermission
import com.momosi.trucktrack.core.push.permission.NotificationPermissionStatus
import com.momosi.trucktrack.user.AuthManager
import com.momosi.trucktrack.user.model.AuthActionResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NotificationPermissionViewModel(private val notificationPermission: NotificationPermission, private val authManager: AuthManager, private val errorReporter: ErrorReporter) : ViewModel() {

    private val isSigningOut = MutableStateFlow(false)

    val state: StateFlow<NotificationPermissionState> = combine(
        notificationPermission.currentStatus,
        isSigningOut,
    ) { status, signingOut ->
        NotificationPermissionState(
            requestRoute = status.toRequestRoute(),
            isSigningOut = signingOut,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = NotificationPermissionState(requestRoute = notificationPermission.currentStatus.value.toRequestRoute()),
    )

    fun onAction(action: NotificationPermissionAction) {
        Logger.d("Action:NotificationPermission", action.toString())
        when (action) {
            NotificationPermissionAction.RefreshPermission -> refreshPermission()
            NotificationPermissionAction.OpenSettings -> notificationPermission.openSettings()
            NotificationPermissionAction.SignOut -> signOut()
        }
    }

    private fun refreshPermission() {
        viewModelScope.launch { notificationPermission.status() }
    }

    private fun signOut() {
        if (isSigningOut.value) return
        viewModelScope.launch {
            isSigningOut.value = true
            if (authManager.signOut() == AuthActionResult.Failed.NoInternet) {
                errorReporter.report(ApiException.NoConnection)
            }
            isSigningOut.value = false
        }
    }
}

private fun NotificationPermissionStatus?.toRequestRoute(): PermissionRequestRoute = when (this) {
    NotificationPermissionStatus.Denied -> PermissionRequestRoute.Settings
    else -> PermissionRequestRoute.SystemDialog
}
