package com.momosi.trucktrack.core.vehicle.model

data class CategorizedVehicles(val preferredTruck: Vehicle?, val preferredTrailer: Vehicle?, val otherVehicles: List<Vehicle>, val defaultVehicle: Vehicle?)
