package com.momosi.trucktrack.core.vehicle

import com.momosi.trucktrack.core.common.coroutines.runCatchingCancellable
import com.momosi.trucktrack.core.common.logger.Logger
import com.momosi.trucktrack.core.vehicle.api.VehicleApi
import com.momosi.trucktrack.core.vehicle.dto.toVehicle
import com.momosi.trucktrack.core.vehicle.model.Vehicle

private const val TAG = "Vehicles"

class VehicleRepositoryImpl(private val vehicleApi: VehicleApi) : VehicleRepository {

    override suspend fun getVehicles(): Result<List<Vehicle>> = runCatchingCancellable {
        vehicleApi.getVehicleList().map { it.toVehicle() }
    }.onFailure { Logger.e(TAG, it, "Failed to get vehicles") }
}
