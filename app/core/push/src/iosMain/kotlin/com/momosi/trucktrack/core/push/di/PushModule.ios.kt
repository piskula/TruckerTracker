package com.momosi.trucktrack.core.push.di

import com.momosi.trucktrack.core.push.firebase.FirebaseInstallationIdProvider
import com.momosi.trucktrack.core.push.firebase.IosPushMessagingClient
import com.momosi.trucktrack.core.push.firebase.PushMessagingClient
import com.momosi.trucktrack.core.push.permission.IosNotificationPermission
import com.momosi.trucktrack.core.push.permission.NotificationPermission
import com.momosi.trucktrack.core.push.registration.IosPushDeviceInfo
import com.momosi.trucktrack.core.push.registration.IosPushRegistrationStorage
import com.momosi.trucktrack.core.push.registration.PushDeviceInfo
import com.momosi.trucktrack.core.push.registration.PushRegistrationStorage
import org.koin.core.module.Module
import org.koin.dsl.binds
import org.koin.dsl.module

actual fun platformPushModule(): Module = module {
    single<PushRegistrationStorage> { IosPushRegistrationStorage.inBackupExcludedDirectory() }
    single<PushDeviceInfo> { IosPushDeviceInfo() }
    single { IosPushMessagingClient() } binds arrayOf(PushMessagingClient::class, FirebaseInstallationIdProvider::class)
    single<NotificationPermission> { IosNotificationPermission() }
}
