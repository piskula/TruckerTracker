package com.momosi.trucktrack.feature.notificationpermission.impl

import androidx.compose.runtime.Immutable

@Immutable
data class NotificationPermissionState(val requestRoute: PermissionRequestRoute = PermissionRequestRoute.SystemDialog, val isSigningOut: Boolean = false)

enum class PermissionRequestRoute {
    SystemDialog,
    Settings,
}
