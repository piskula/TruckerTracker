package com.momosi.trucktrack.app

import com.momosi.trucktrack.core.push.permission.NotificationPermissionStatus
import com.momosi.trucktrack.feature.issues.api.IssuesNavKey
import com.momosi.trucktrack.feature.issues.impl.navigation.IssueDetailNavKey
import com.momosi.trucktrack.feature.notificationpermission.api.NotificationPermissionNavKey
import com.momosi.trucktrack.feature.signin.api.SignInNavKey
import com.momosi.trucktrack.user.model.AuthenticationState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AppAccessTest {

    @Test
    fun signedOutWinsOverThePermission() {
        listOf(null, NotificationPermissionStatus.Granted, NotificationPermissionStatus.CanAsk, NotificationPermissionStatus.Denied).forEach { status ->
            assertEquals(AppAccess.SignedOut, appAccessOf(AuthenticationState.Guest, status))
        }
    }

    @Test
    fun signedInAccessFollowsThePermission() {
        assertEquals(AppAccess.Unknown, appAccessOf(AuthenticationState.Authorized, null))
        assertEquals(AppAccess.Ready, appAccessOf(AuthenticationState.Authorized, NotificationPermissionStatus.Granted))
        assertEquals(AppAccess.NotificationPermissionRequired, appAccessOf(AuthenticationState.Authorized, NotificationPermissionStatus.CanAsk))
        assertEquals(AppAccess.NotificationPermissionRequired, appAccessOf(AuthenticationState.Authorized, NotificationPermissionStatus.Denied))
    }

    @Test
    fun blockedAccessMovesToItsRootFromAnywhereElse() {
        assertEquals(SignInNavKey, AppAccess.SignedOut.redirectFrom(IssueDetailNavKey(7)))
        assertNull(AppAccess.SignedOut.redirectFrom(SignInNavKey))
        assertEquals(NotificationPermissionNavKey, AppAccess.NotificationPermissionRequired.redirectFrom(IssueDetailNavKey(7)))
        assertNull(AppAccess.NotificationPermissionRequired.redirectFrom(NotificationPermissionNavKey))
    }

    @Test
    fun readyAccessLeavesOnlySignInAndTheGate() {
        assertEquals(IssuesNavKey, AppAccess.Ready.redirectFrom(SignInNavKey))
        assertEquals(IssuesNavKey, AppAccess.Ready.redirectFrom(NotificationPermissionNavKey))
        assertNull(AppAccess.Ready.redirectFrom(IssueDetailNavKey(7)))
        assertNull(AppAccess.Unknown.redirectFrom(SignInNavKey))
    }
}
