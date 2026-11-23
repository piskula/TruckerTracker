package com.momosi.trucktrack.app

import androidx.navigation3.runtime.NavKey
import androidx.savedstate.serialization.SavedStateConfiguration
import com.momosi.trucktrack.feature.issues.api.IssuesNavKey
import com.momosi.trucktrack.feature.issues.impl.navigation.CreateIssueNavKey
import com.momosi.trucktrack.feature.issues.impl.navigation.FullScreenPhotoNavKey
import com.momosi.trucktrack.feature.issues.impl.navigation.IssueDetailNavKey
import com.momosi.trucktrack.feature.notificationpermission.api.NotificationPermissionNavKey
import com.momosi.trucktrack.feature.profile.api.ProfileNavKey
import com.momosi.trucktrack.feature.signin.api.SignInNavKey
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

val appSavedStateConfiguration = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(SignInNavKey::class, SignInNavKey.serializer())
            subclass(IssuesNavKey::class, IssuesNavKey.serializer())
            subclass(IssueDetailNavKey::class, IssueDetailNavKey.serializer())
            subclass(CreateIssueNavKey::class, CreateIssueNavKey.serializer())
            subclass(FullScreenPhotoNavKey::class, FullScreenPhotoNavKey.serializer())
            subclass(ProfileNavKey::class, ProfileNavKey.serializer())
            subclass(NotificationPermissionNavKey::class, NotificationPermissionNavKey.serializer())
        }
    }
}
