package com.momosi.trucktrack.core.applinks.di

import com.momosi.trucktrack.core.applinks.AppLinksManager
import com.momosi.trucktrack.core.applinks.AppLinksManagerImpl
import org.koin.dsl.module

val appLinksModule = module {
    single<AppLinksManager> { AppLinksManagerImpl(get()) }
}
