package com.momosi.trucktrack.feature.profile.impl

import androidx.compose.runtime.Immutable
import com.momosi.trucktrack.core.common.language.AppLanguage
import com.momosi.trucktrack.core.common.network.FailureReason
import com.momosi.trucktrack.user.model.User

@Immutable
data class ProfileState(
    val user: User? = null,
    val isSigningOut: Boolean = false,
    val appVersion: String = "",
    val isVersionDialogVisible: Boolean = false,
    val serverVersion: ServerVersionContent = ServerVersionContent.Loading,
    val firebaseInstallationId: FirebaseInstallationIdContent = FirebaseInstallationIdContent.Loading,
    val language: AppLanguage = AppLanguage.English,
    val isLanguageDialogVisible: Boolean = false,
    val isRestartNoticeVisible: Boolean = false,
    val pushNotifications: PushNotificationsState = PushNotificationsState(),
)

sealed interface FirebaseInstallationIdContent {
    data object Loading : FirebaseInstallationIdContent
    data class Loaded(val value: String) : FirebaseInstallationIdContent
    data object Unavailable : FirebaseInstallationIdContent
}

sealed interface ServerVersionContent {
    data object Loading : ServerVersionContent
    data class Loaded(val text: String) : ServerVersionContent
    data object Error : ServerVersionContent
}

@Immutable
data class PushNotificationsState(val target: PushNotificationsTarget = PushNotificationsTarget.Loading, val request: PushNotificationsRequest = PushNotificationsRequest.Idle) {
    val receivesOnThisDevice: Boolean
        get() = target == PushNotificationsTarget.ThisDevice

    val canChange: Boolean
        get() = request == PushNotificationsRequest.Idle &&
            target !is PushNotificationsTarget.Loading &&
            target !is PushNotificationsTarget.LoadFailed
}

sealed interface PushNotificationsTarget {
    data object Loading : PushNotificationsTarget
    data class LoadFailed(val reason: FailureReason) : PushNotificationsTarget
    data object ThisDevice : PushNotificationsTarget
    data class OtherDevice(val deviceName: String) : PushNotificationsTarget
    data object NoDevice : PushNotificationsTarget
}

sealed interface PushNotificationsRequest {
    data object Idle : PushNotificationsRequest
    data object Updating : PushNotificationsRequest
    data class ConfirmingTakeover(val currentDeviceName: String) : PushNotificationsRequest
}
