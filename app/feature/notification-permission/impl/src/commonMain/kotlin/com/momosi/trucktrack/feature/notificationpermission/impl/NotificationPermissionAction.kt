package com.momosi.trucktrack.feature.notificationpermission.impl

sealed interface NotificationPermissionAction {
    data object RefreshPermission : NotificationPermissionAction
    data object OpenSettings : NotificationPermissionAction
    data object SignOut : NotificationPermissionAction
}
