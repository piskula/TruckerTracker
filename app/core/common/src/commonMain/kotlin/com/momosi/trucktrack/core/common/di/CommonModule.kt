package com.momosi.trucktrack.core.common.di

import com.momosi.trucktrack.core.common.coroutines.DispatcherProvider
import com.momosi.trucktrack.core.common.error.ErrorReporter
import com.momosi.trucktrack.core.common.error.ErrorReporterImpl
import com.momosi.trucktrack.core.common.formatter.DateFormatter
import com.momosi.trucktrack.core.common.version.VersionRepository
import com.momosi.trucktrack.core.common.version.VersionRepositoryImpl
import com.momosi.trucktrack.core.common.version.api.VersionApi
import org.koin.core.module.Module
import org.koin.dsl.module

val commonModule = module {
    single { DispatcherProvider() }
    single { DateFormatter() }
    single { VersionApi() }
    single<VersionRepository> { VersionRepositoryImpl(get()) }
    single<ErrorReporter> { ErrorReporterImpl() }
}

expect fun platformCommonModule(): Module
