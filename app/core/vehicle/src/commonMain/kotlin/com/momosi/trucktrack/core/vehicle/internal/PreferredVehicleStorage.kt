package com.momosi.trucktrack.core.vehicle.internal

import com.momosi.trucktrack.core.vehicle.model.VehicleType

const val VEHICLE_PREFERENCE_STORAGE = "vehicle_preference_storage"

interface PreferredVehicleStorage {
    fun getPreferredVehicleId(userId: String, type: VehicleType): Long?
    fun setPreferredVehicleId(
        userId: String,
        type: VehicleType,
        vehicleId: Long,
    )
    fun getMostRecentlyUsedType(userId: String): VehicleType?
}
