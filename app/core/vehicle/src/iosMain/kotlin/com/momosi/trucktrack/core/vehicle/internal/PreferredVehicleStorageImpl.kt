package com.momosi.trucktrack.core.vehicle.internal

import com.momosi.trucktrack.core.vehicle.model.VehicleType
import platform.Foundation.NSUserDefaults

class PreferredVehicleStorageImpl(private val defaults: NSUserDefaults) : PreferredVehicleStorage {

    override fun getPreferredVehicleId(userId: String, type: VehicleType): Long? = defaults.stringForKey(vehicleIdKey(userId, type))?.toLongOrNull()

    override fun setPreferredVehicleId(
        userId: String,
        type: VehicleType,
        vehicleId: Long,
    ) {
        defaults.setObject(vehicleId.toString(), vehicleIdKey(userId, type))
    }
}

private fun vehicleIdKey(userId: String, type: VehicleType) = "${userId}_${type.toStorageKey()}_vehicle_id"

private fun VehicleType.toStorageKey(): String = when (this) {
    VehicleType.Truck -> "TRUCK"
    VehicleType.Trailer -> "TRAILER"
}
