package com.momosi.trucktrack.core.push.di

import com.momosi.trucktrack.core.push.message.PushMessageDispatcher
import com.momosi.trucktrack.core.push.message.PushMessageDispatcherImpl
import com.momosi.trucktrack.core.push.message.PushMessageHandler
import com.momosi.trucktrack.core.push.registration.DeviceUnregisteredHandler
import com.momosi.trucktrack.core.push.registration.PushDeviceRegistrar
import com.momosi.trucktrack.core.push.registration.PushDeviceRegistrarImpl
import com.momosi.trucktrack.core.push.registration.PushRegistrationManager
import com.momosi.trucktrack.core.push.registration.PushRegistrationManagerImpl
import com.momosi.trucktrack.core.push.registration.api.createPushDeviceApi
import de.jensklingenberg.ktorfit.Ktorfit
import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.module

expect fun platformPushModule(): Module

val pushModule = module {
    single { get<Ktorfit>().createPushDeviceApi() }
    single<PushDeviceRegistrar> { PushDeviceRegistrarImpl(get(), get(), get(), get()) }
    single<PushRegistrationManager>(createdAtStart = true) { PushRegistrationManagerImpl(get(), get(), get(), get(), get()) }
    single { DeviceUnregisteredHandler(get()) } bind PushMessageHandler::class
    single(createdAtStart = true) { PushMessageDispatcherImpl(get(), getAll<PushMessageHandler>(), get()) } bind PushMessageDispatcher::class
}
