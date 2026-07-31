package com.momosi.trucktrack.feature.profile.impl.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.momosi.trucktrack.core.navigation.Navigator
import com.momosi.trucktrack.core.uilibrary.animation.slideFromEndEntryMetadata
import com.momosi.trucktrack.feature.profile.api.ProfileNavKey
import com.momosi.trucktrack.feature.profile.impl.ProfileScreen
import com.momosi.trucktrack.feature.signin.api.SignInNavKey

fun EntryProviderScope<NavKey>.profileEntries(navigator: Navigator) {
    entry<ProfileNavKey>(metadata = slideFromEndEntryMetadata()) {
        ProfileScreen(
            onNavigateToSignIn = { navigator.navigateToRoot(SignInNavKey) },
            onBack = navigator::goBack,
        )
    }
}
