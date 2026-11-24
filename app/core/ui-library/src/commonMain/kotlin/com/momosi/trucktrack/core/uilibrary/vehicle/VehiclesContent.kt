package com.momosi.trucktrack.core.uilibrary.vehicle

import androidx.compose.runtime.Immutable
import com.momosi.trucktrack.core.vehicle.model.CategorizedVehicles
import com.momosi.trucktrack.core.vehicle.model.Vehicle
import com.momosi.trucktrack.core.vehicle.model.VehicleType
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

@Immutable
sealed interface VehiclesContent {
    data object Loading : VehiclesContent

    @Immutable
    data class Loaded(val preferredTruck: Vehicle? = null, val preferredTrailer: Vehicle? = null, val otherVehicles: ImmutableList<Vehicle> = persistentListOf()) : VehiclesContent

    data object Error : VehiclesContent
}

fun CategorizedVehicles.toVehiclesContent(): VehiclesContent.Loaded = VehiclesContent.Loaded(
    preferredTruck = preferredTruck,
    preferredTrailer = preferredTrailer,
    otherVehicles = otherVehicles.toImmutableList(),
)

fun CategorizedVehicles.toVehiclesContent(type: VehicleType): VehiclesContent.Loaded = VehiclesContent.Loaded(
    preferredTruck = preferredTruck.takeIf { type == VehicleType.Truck },
    preferredTrailer = preferredTrailer.takeIf { type == VehicleType.Trailer },
    otherVehicles = otherVehicles.filter { it.type == type }.toImmutableList(),
)
