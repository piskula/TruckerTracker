package com.momosi.trucktrack.core.vehicle.di

import com.momosi.trucktrack.core.vehicle.internal.PreferredVehicleStorage
import com.momosi.trucktrack.core.vehicle.internal.PreferredVehicleStorageImpl
import org.koin.core.module.Module
import org.koin.dsl.module
import platform.Foundation.NSUserDefaults

actual fun platformVehicleModule(): Module = module {
    single<PreferredVehicleStorage> { PreferredVehicleStorageImpl(NSUserDefaults.standardUserDefaults) }
}
