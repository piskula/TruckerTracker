package com.momosi.trucktrack.core.issue.model

enum class VehicleSystem {
    Electrical,
    Tires,
    Body,
    Engine,
    Drivetrain,
    Brakes,
    Air,
    Tarp,
    Cooling,
    Other,
    ;

    companion object {
        fun fromApiValue(value: String): VehicleSystem = when (value) {
            "ELECTRICAL" -> Electrical
            "TIRES" -> Tires
            "BODY" -> Body
            "ENGINE" -> Engine
            "DRIVETRAIN" -> Drivetrain
            "BRAKES" -> Brakes
            "AIR" -> Air
            "TARP" -> Tarp
            "COOLING" -> Cooling
            "OTHER" -> Other
            else -> Other
        }
    }

    fun toApiValue(): String = when (this) {
        Electrical -> "ELECTRICAL"
        Tires -> "TIRES"
        Body -> "BODY"
        Engine -> "ENGINE"
        Drivetrain -> "DRIVETRAIN"
        Brakes -> "BRAKES"
        Air -> "AIR"
        Tarp -> "TARP"
        Cooling -> "COOLING"
        Other -> "OTHER"
    }
}
