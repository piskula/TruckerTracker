package com.momosi.trucktrack.core.push.permission

import com.momosi.trucktrack.core.push.firebase.IosPushEvents
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenSettingsURLString
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionBadge
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNAuthorizationStatus
import platform.UserNotifications.UNAuthorizationStatusAuthorized
import platform.UserNotifications.UNAuthorizationStatusEphemeral
import platform.UserNotifications.UNAuthorizationStatusNotDetermined
import platform.UserNotifications.UNAuthorizationStatusProvisional
import platform.UserNotifications.UNUserNotificationCenter
import kotlin.coroutines.resume

internal class IosNotificationPermission : NotificationPermission {

    override val currentStatus: StateFlow<NotificationPermissionStatus?>
        field = MutableStateFlow<NotificationPermissionStatus?>(null)

    override suspend fun status(): NotificationPermissionStatus = authorizationStatus().toPermissionStatus().also { currentStatus.value = it }

    override fun openSettings() {
        val url = NSURL.URLWithString(IosPushEvents.notificationSettingsUrl ?: UIApplicationOpenSettingsURLString) ?: return
        UIApplication.sharedApplication.openURL(url, emptyMap<Any?, Any>(), null)
    }
}

internal suspend fun requestNotificationAuthorization(): Boolean = suspendCancellableCoroutine { continuation ->
    UNUserNotificationCenter.currentNotificationCenter().requestAuthorizationWithOptions(
        UNAuthorizationOptionAlert or UNAuthorizationOptionSound or UNAuthorizationOptionBadge,
    ) { granted, _ ->
        continuation.resume(granted)
    }
}

private suspend fun authorizationStatus(): UNAuthorizationStatus = suspendCancellableCoroutine { continuation ->
    UNUserNotificationCenter.currentNotificationCenter().getNotificationSettingsWithCompletionHandler { settings ->
        continuation.resume(settings?.authorizationStatus ?: UNAuthorizationStatusNotDetermined)
    }
}

private fun UNAuthorizationStatus.toPermissionStatus(): NotificationPermissionStatus = when (this) {
    UNAuthorizationStatusAuthorized,
    UNAuthorizationStatusProvisional,
    UNAuthorizationStatusEphemeral,
    -> NotificationPermissionStatus.Granted

    UNAuthorizationStatusNotDetermined -> NotificationPermissionStatus.CanAsk

    else -> NotificationPermissionStatus.Denied
}
