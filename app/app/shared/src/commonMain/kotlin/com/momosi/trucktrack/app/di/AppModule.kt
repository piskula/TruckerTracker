package com.momosi.trucktrack.app.di

import com.momosi.trucktrack.app.CrashReportingSync
import com.momosi.trucktrack.app.TruckTrackViewModel
import com.momosi.trucktrack.core.applinks.di.appLinksModule
import com.momosi.trucktrack.core.checklist.di.checklistModule
import com.momosi.trucktrack.core.common.di.commonModule
import com.momosi.trucktrack.core.common.di.platformCommonModule
import com.momosi.trucktrack.core.issue.di.issueModule
import com.momosi.trucktrack.core.network.di.networkModule
import com.momosi.trucktrack.core.vehicle.di.platformVehicleModule
import com.momosi.trucktrack.core.vehicle.di.vehicleModule
import com.momosi.trucktrack.feature.checklist.impl.di.checklistFeatureModule
import com.momosi.trucktrack.feature.issues.impl.di.issuesModule
import com.momosi.trucktrack.feature.profile.impl.di.profileModule
import com.momosi.trucktrack.feature.signin.impl.di.signInModule
import com.momosi.trucktrack.user.di.platformUserModule
import com.momosi.trucktrack.user.di.userModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single { CoroutineScope(SupervisorJob()) }
    single(createdAtStart = true) { CrashReportingSync(get(), get(), get()) }
    viewModel { TruckTrackViewModel(get(), get(), get()) }
}

val allModules: List<Module> = listOf(
    commonModule,
    platformCommonModule(),
    networkModule,
    userModule,
    platformUserModule(),
    appLinksModule,
    vehicleModule,
    platformVehicleModule(),
    issueModule,
    checklistModule,
    appModule,
    signInModule,
    issuesModule,
    profileModule,
    checklistFeatureModule,
)
