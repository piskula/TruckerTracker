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
            .putString(mostRecentTypeKey(userId), type.toStorageKey())
            .apply()
    }

    override fun getMostRecentlyUsedType(userId: String): VehicleType? = preferences.getString(mostRecentTypeKey(userId), null)?.toVehicleTypeFromStorageKey()
}

private fun vehicleIdKey(userId: String, type: VehicleType) = "${userId}_${type.toStorageKey()}_vehicle_id"
private fun mostRecentTypeKey(userId: String) = "${userId}_most_recent_vehicle_type"

private fun VehicleType.toStorageKey(): String = when (this) {
    VehicleType.Truck -> "TRUCK"
    VehicleType.Trailer -> "TRAILER"
}

private fun String.toVehicleTypeFromStorageKey(): VehicleType? = when (this) {
    "TRUCK" -> VehicleType.Truck
    "TRAILER" -> VehicleType.Trailer
    else -> null
}
