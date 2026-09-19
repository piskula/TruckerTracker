package com.momosi.trucktrack.core.vehicle.di

import android.content.Context
import com.momosi.trucktrack.core.vehicle.internal.PreferredVehicleStorage
import com.momosi.trucktrack.core.vehicle.internal.PreferredVehicleStorageImpl
import com.momosi.trucktrack.core.vehicle.internal.VEHICLE_PREFERENCE_STORAGE
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module

actual fun platformVehicleModule(): Module = module {
    single(named(VEHICLE_PREFERENCE_STORAGE)) {
        get<Context>().getSharedPreferences(VEHICLE_PREFERENCE_STORAGE, Context.MODE_PRIVATE)
    }
    single<PreferredVehicleStorage> { PreferredVehicleStorageImpl(get(named(VEHICLE_PREFERENCE_STORAGE))) }
}
