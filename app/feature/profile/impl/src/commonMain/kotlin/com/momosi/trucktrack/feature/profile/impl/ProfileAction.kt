package com.momosi.trucktrack.feature.profile.impl

import com.momosi.trucktrack.core.common.language.AppLanguage

sealed interface ProfileAction {
    data object SignOut : ProfileAction
    data object ShowVersionInfo : ProfileAction
    data object DismissVersionInfo : ProfileAction
    data object TapAppVersion : ProfileAction
    data object ShowLanguageSelector : ProfileAction
    data object DismissLanguageSelector : ProfileAction
    data class SelectLanguage(val language: AppLanguage) : ProfileAction
    data object DismissRestartNotice : ProfileAction
    data class ChangeReceivePushNotifications(val enabled: Boolean) : ProfileAction
    data object ConfirmPushNotificationsTakeover : ProfileAction
    data object DismissPushNotificationsTakeover : ProfileAction
    data object RetryPushNotifications : ProfileAction
}
