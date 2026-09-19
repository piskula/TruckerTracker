package com.momosi.trucktrack.shared.issue

import kotlinx.serialization.Serializable

@Serializable
enum class VehicleSystemDto {
    ELECTRICAL,
    TIRES,
    BODY,
    ENGINE,
    DRIVETRAIN,
    BRAKES,
    AIR,
    TARP,
    COOLING,
    OTHER,
}
