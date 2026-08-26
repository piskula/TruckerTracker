package com.momosi.trucktrack.core.common.di

import android.content.Context
import com.momosi.trucktrack.core.common.language.LanguageRepository
import com.momosi.trucktrack.core.common.language.LanguageRepositoryImpl
import com.momosi.trucktrack.core.common.network.ConnectivityManager
import com.momosi.trucktrack.core.common.network.ConnectivityManagerImpl
import com.momosi.trucktrack.core.common.version.AppVersionProvider
import com.momosi.trucktrack.core.common.version.AppVersionProviderImpl
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun platformCommonModule(): Module = module {
    single { get<Context>().getSystemService(Context.CONNECTIVITY_SERVICE) as android.net.ConnectivityManager }
    single<ConnectivityManager> { ConnectivityManagerImpl(get()) }
    single<AppVersionProvider> { AppVersionProviderImpl(get()) }
    single<LanguageRepository> { LanguageRepositoryImpl() }
}
