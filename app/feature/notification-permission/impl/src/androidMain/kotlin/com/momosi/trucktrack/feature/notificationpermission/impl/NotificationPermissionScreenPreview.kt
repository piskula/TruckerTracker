package com.momosi.trucktrack.feature.notificationpermission.impl

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.momosi.trucktrack.core.uilibrary.theme.TruckTrackTheme

@Preview
@Composable
private fun NotificationPermissionSystemDialogPreview() {
    TruckTrackTheme {
        NotificationPermissionContent(
            state = NotificationPermissionState(requestRoute = PermissionRequestRoute.SystemDialog),
            onRequestPermission = {},
            onAction = {},
        )
    }
}

@Preview
@Composable
private fun NotificationPermissionSettingsPreview() {
    TruckTrackTheme {
        NotificationPermissionContent(
            state = NotificationPermissionState(requestRoute = PermissionRequestRoute.Settings),
            onRequestPermission = {},
            onAction = {},
        )
    }
}

@Preview
@Composable
private fun NotificationPermissionSigningOutPreview() {
    TruckTrackTheme {
        NotificationPermissionContent(
            state = NotificationPermissionState(requestRoute = PermissionRequestRoute.Settings, isSigningOut = true),
            onRequestPermission = {},
            onAction = {},
        )
    }
}
