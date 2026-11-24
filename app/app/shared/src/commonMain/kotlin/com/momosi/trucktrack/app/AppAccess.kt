package com.momosi.trucktrack.app

import androidx.navigation3.runtime.NavKey
import com.momosi.trucktrack.core.push.permission.NotificationPermissionStatus
import com.momosi.trucktrack.feature.issues.api.IssuesNavKey
import com.momosi.trucktrack.feature.notificationpermission.api.NotificationPermissionNavKey
import com.momosi.trucktrack.feature.signin.api.SignInNavKey
import com.momosi.trucktrack.user.model.AuthenticationState

internal enum class AppAccess {
    Unknown,
    SignedOut,
    NotificationPermissionRequired,
    Ready,
}

internal fun appAccessOf(authenticationState: AuthenticationState, notificationPermissionStatus: NotificationPermissionStatus?): AppAccess = when {
    authenticationState == AuthenticationState.Guest -> AppAccess.SignedOut
    notificationPermissionStatus == null -> AppAccess.Unknown
    notificationPermissionStatus == NotificationPermissionStatus.Granted -> AppAccess.Ready
    else -> AppAccess.NotificationPermissionRequired
}

internal fun AppAccess.startKey(): NavKey = when (this) {
    AppAccess.SignedOut -> SignInNavKey
    AppAccess.NotificationPermissionRequired -> NotificationPermissionNavKey
    AppAccess.Unknown, AppAccess.Ready -> IssuesNavKey
}

internal fun AppAccess.redirectFrom(currentKey: NavKey): NavKey? = when (this) {
    AppAccess.Unknown -> null
    AppAccess.SignedOut -> SignInNavKey.takeUnless { currentKey == SignInNavKey }
    AppAccess.NotificationPermissionRequired -> NotificationPermissionNavKey.takeUnless { currentKey == NotificationPermissionNavKey }
    AppAccess.Ready -> IssuesNavKey.takeIf { currentKey == SignInNavKey || currentKey == NotificationPermissionNavKey }
}
