package com.momosi.trucktrack.core.push.di

import android.content.Context
import com.momosi.trucktrack.core.push.firebase.FirebaseInstallationIdProvider
import com.momosi.trucktrack.core.push.firebase.FirebasePushMessagingClient
import com.momosi.trucktrack.core.push.firebase.PushMessagingClient
import com.momosi.trucktrack.core.push.firebase.ReceivedFirebaseInstallationIdStorage
import com.momosi.trucktrack.core.push.message.PushNotificationPresenter
import com.momosi.trucktrack.core.push.permission.AndroidNotificationPermission
import com.momosi.trucktrack.core.push.permission.NotificationPermission
import com.momosi.trucktrack.core.push.permission.NotificationPermissionPromptHistory
import com.momosi.trucktrack.core.push.registration.AndroidPushDeviceInfo
import com.momosi.trucktrack.core.push.registration.AndroidPushRegistrationStorage
import com.momosi.trucktrack.core.push.registration.PUSH_REGISTRATION_STORAGE
import com.momosi.trucktrack.core.push.registration.PushDeviceInfo
import com.momosi.trucktrack.core.push.registration.PushRegistrationStorage
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.binds
import org.koin.dsl.module

actual fun platformPushModule(): Module = module {
    single(named(PUSH_REGISTRATION_STORAGE)) {
        get<Context>().getSharedPreferences(PUSH_REGISTRATION_STORAGE, Context.MODE_PRIVATE)
    }
    single<PushRegistrationStorage> { AndroidPushRegistrationStorage(get(named(PUSH_REGISTRATION_STORAGE))) }
    single { ReceivedFirebaseInstallationIdStorage(get(named(PUSH_REGISTRATION_STORAGE))) }
    single { NotificationPermissionPromptHistory(get(named(PUSH_REGISTRATION_STORAGE))) }
    single<PushDeviceInfo> { AndroidPushDeviceInfo(get()) }
    single { FirebasePushMessagingClient(get()) } binds arrayOf(PushMessagingClient::class, FirebaseInstallationIdProvider::class)
    single<NotificationPermission> { AndroidNotificationPermission(get(), get()) }
    single(createdAtStart = true) { PushNotificationPresenter(get()).apply { createChannel() } }
}
