package com.momosi.trucktrack.feature.profile.impl.di

import com.momosi.trucktrack.feature.profile.impl.ProfileViewModel
import com.momosi.trucktrack.feature.profile.impl.TestCrashManager
import com.momosi.trucktrack.feature.profile.impl.TestCrashManagerImpl
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val profileModule = module {
    single<TestCrashManager> { TestCrashManagerImpl() }
    viewModel { ProfileViewModel(get(), get(), get(), get(), get(), get()) }
}
