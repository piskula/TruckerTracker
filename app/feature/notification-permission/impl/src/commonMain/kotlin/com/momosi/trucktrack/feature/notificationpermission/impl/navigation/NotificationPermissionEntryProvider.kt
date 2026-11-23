package com.momosi.trucktrack.feature.notificationpermission.impl.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.momosi.trucktrack.feature.notificationpermission.api.NotificationPermissionNavKey
import com.momosi.trucktrack.feature.notificationpermission.impl.NotificationPermissionScreen

fun EntryProviderScope<NavKey>.notificationPermissionEntries() {
    entry<NotificationPermissionNavKey> {
        NotificationPermissionScreen()
    }
}
