package com.momosi.trucktrack.feature.issues.impl

import com.momosi.trucktrack.core.vehicle.model.CategorizedVehicles
import kotlinx.collections.immutable.toImmutableList

fun CategorizedVehicles.toVehiclesContent(): VehiclesContent.Loaded = VehiclesContent.Loaded(
    preferredTruck = preferredTruck,
    preferredTrailer = preferredTrailer,
    otherVehicles = otherVehicles.toImmutableList(),
)
