package com.momosi.trucktrack.feature.signin.impl.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.momosi.trucktrack.feature.signin.api.SignInNavKey
import com.momosi.trucktrack.feature.signin.impl.SignInScreen

fun EntryProviderScope<NavKey>.signInEntries() {
    entry<SignInNavKey> {
        SignInScreen()
    }
}
