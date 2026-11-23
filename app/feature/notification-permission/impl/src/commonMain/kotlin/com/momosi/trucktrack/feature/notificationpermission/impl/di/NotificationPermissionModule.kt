package com.momosi.trucktrack.feature.notificationpermission.impl.di

import com.momosi.trucktrack.feature.notificationpermission.impl.NotificationPermissionViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val notificationPermissionModule = module {
    viewModel { NotificationPermissionViewModel(get(), get(), get()) }
}
