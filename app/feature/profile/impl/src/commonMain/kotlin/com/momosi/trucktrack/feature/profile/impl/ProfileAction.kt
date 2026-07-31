package com.momosi.trucktrack.feature.profile.impl

sealed interface ProfileAction {
    data object SignOut : ProfileAction
    data object ShowVersionInfo : ProfileAction
    data object DismissVersionInfo : ProfileAction
    data object TapAppVersion : ProfileAction
}
