package com.momosi.trucktrack.core.vehicle.internal

import android.content.SharedPreferences
import com.momosi.trucktrack.core.vehicle.model.VehicleType

class PreferredVehicleStorageImpl(private val preferences: SharedPreferences) : PreferredVehicleStorage {

    override fun getPreferredVehicleId(userId: String, type: VehicleType): Long? = preferences.getString(vehicleIdKey(userId, type), null)?.toLongOrNull()

    override fun setPreferredVehicleId(
        userId: String,
        type: VehicleType,
        vehicleId: Long,
    ) {
        preferences.edit()
            .putString(vehicleIdKey(userId, type), vehicleId.toString())
            .apply()
    }
}

private fun vehicleIdKey(userId: String, type: VehicleType) = "${userId}_${type.toStorageKey()}_vehicle_id"

private fun VehicleType.toStorageKey(): String = when (this) {
    VehicleType.Truck -> "TRUCK"
    VehicleType.Trailer -> "TRAILER"
}
