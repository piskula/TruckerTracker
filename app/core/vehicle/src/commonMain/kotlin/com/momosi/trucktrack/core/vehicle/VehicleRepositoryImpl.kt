package com.momosi.trucktrack.core.vehicle

import com.momosi.trucktrack.core.common.coroutines.runCatchingCancellable
import com.momosi.trucktrack.core.common.logger.Logger
import com.momosi.trucktrack.core.common.network.onNetworkFailure
import com.momosi.trucktrack.core.common.network.onNoConnectionFailure
import com.momosi.trucktrack.core.vehicle.api.VehicleApi
import com.momosi.trucktrack.core.vehicle.dto.toVehicle
import com.momosi.trucktrack.core.vehicle.internal.PreferredVehicleStorage
import com.momosi.trucktrack.core.vehicle.model.CategorizedVehicles
import com.momosi.trucktrack.core.vehicle.model.Vehicle
import com.momosi.trucktrack.core.vehicle.model.VehicleType
import com.momosi.trucktrack.user.UserRepository
import com.momosi.trucktrack.user.model.UserRole

private const val TAG = "Vehicles"

class VehicleRepositoryImpl(private val vehicleApi: VehicleApi, private val preferredVehicleStorage: PreferredVehicleStorage, private val userRepository: UserRepository) : VehicleRepository {

    override suspend fun getVehicles(): Result<CategorizedVehicles> = runCatchingCancellable {
        val vehicles = vehicleApi.getVehicleList().map { it.toVehicle() }.sortedBy { it.licensePlate }
        categorize(vehicles)
    }
        .onNoConnectionFailure { Logger.w(TAG, it, "Failed to get vehicles (offline)") }
        .onNetworkFailure { Logger.e(TAG, it, "Failed to get vehicles") }

    override fun recordVehicleUsage(vehicle: Vehicle) {
        val userId = currentDriverUserId() ?: return
        preferredVehicleStorage.setPreferredVehicleId(userId, vehicle.type, vehicle.id)
    }

    private fun currentDriverUserId(): String? {
        val user = userRepository.user.value ?: return null
        return user.id.takeIf { UserRole.Driver in user.roles }
    }

    private fun categorize(vehicles: List<Vehicle>): CategorizedVehicles {
        val userId = currentDriverUserId()
        val preferredTruck = userId?.let { preferredVehicleStorage.getPreferredVehicleId(it, VehicleType.Truck) }
            ?.let { id -> vehicles.firstOrNull { it.id == id } }
        val preferredTrailer = userId?.let { preferredVehicleStorage.getPreferredVehicleId(it, VehicleType.Trailer) }
            ?.let { id -> vehicles.firstOrNull { it.id == id } }
        val preferredIds = setOfNotNull(preferredTruck?.id, preferredTrailer?.id)
        val mostRecentlyUsedType = userId?.let { preferredVehicleStorage.getMostRecentlyUsedType(it) }

        return CategorizedVehicles(
            preferredTruck = preferredTruck,
            preferredTrailer = preferredTrailer,
            otherVehicles = vehicles.filterNot { it.id in preferredIds },
            defaultVehicle = when (mostRecentlyUsedType) {
                VehicleType.Truck -> preferredTruck
                VehicleType.Trailer -> preferredTrailer
                null -> null
            },
        )
    }
}
